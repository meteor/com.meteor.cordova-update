#import "METEORCordovaURLProtocol.h"

NSString *METEORDocumentRoot;
NSString *METEORCordovajsRoot;

@protocol METEORCordovaURLProtocol

- (NSString *)filePathForURI:(NSString *)path allowDirectory:(BOOL)allowDirectory;
- (NSArray *)directoryIndexFileNames;

@end

@implementation METEORCordovaURLProtocol

+ (BOOL)canInitWithRequest:(NSURLRequest *)request
{
  // only handle http requests originated to "meteor.local" domain
  if ([[[request URL] scheme] isEqualToString:@"http"] &&
      [[[request URL] host] isEqualToString:@"meteor.local"]) {
    return YES;
  }
  return NO;
}

+ (NSURLRequest *)canonicalRequestForRequest:(NSURLRequest *)request
{
  return request;
}


- (void)startLoading
{
  NSLog(@"startLoading for url %@", [[[self request] URL] absoluteString]);
  NSString *filePath = [self filePathForURI:[[[self request] URL] path] allowDirectory:NO];

  BOOL isDir = NO;

  // XXX HACKHACK if the file not found, return the root page
  if (!filePath || ![[NSFileManager defaultManager] fileExistsAtPath:filePath isDirectory:&isDir] || isDir)
  {
    filePath = [self filePathForURI:@"/" allowDirectory:NO];
  }

  NSLog(@"filePath is %@", filePath);

  NSData *data = [NSData dataWithContentsOfURL:[NSURL URLWithString:filePath]];

  NSHTTPURLResponse *response = [[NSHTTPURLResponse alloc] initWithURL:[[self request] URL] statusCode:200 HTTPVersion:@"HTTP/1.1" headerFields:@{}];

  [[self client] URLProtocol:self didReceiveResponse:response cacheStoragePolicy:NSURLCacheStorageNotAllowed]; // we handle caching ourselves.
  [[self client] URLProtocol:self didLoadData:data];
  [[self client] URLProtocolDidFinishLoading:self];
}

- (void)stopLoading
{
  // No-op
}

/**
 * Converts relative URI path into full file-system path.
**/
- (NSString *)filePathForURI:(NSString *)path allowDirectory:(BOOL)allowDirectory
{
  NSString *documentRoot = METEORDocumentRoot;

  // Part 1: Strip parameters from the url
  // E.g.: /page.html?q=22&var=abc -> /page.html

  NSURL *docRoot = [NSURL fileURLWithPath:documentRoot isDirectory:YES];
  if (docRoot == nil)
  {
    return nil;
  }

  NSString *relativePath = [[NSURL URLWithString:path relativeToURL:docRoot] relativePath];

  // Part 2: Append relative path to document root (base path)
  // E.g.: relativePath="/images/icon.png"
  //       documentRoot="/Users/robbie/Sites"
  //           fullPath="/Users/robbie/Sites/images/icon.png"
  // We also standardize the path.
  // E.g.: "Users/robbie/Sites/images/../index.html" -> "/Users/robbie/Sites/index.html"

  NSString *fullPath = [[documentRoot stringByAppendingPathComponent:relativePath] stringByStandardizingPath];

  if ([relativePath isEqualToString:@"/"])
  {
    fullPath = [fullPath stringByAppendingString:@"/"];
  }

  // Part 3: Prevent serving files outside the document root.
  // Sneaky requests may include ".." in the path.
  // E.g.: relativePath="../Documents/TopSecret.doc"
  //       documentRoot="/Users/robbie/Sites"
  //           fullPath="/Users/robbie/Documents/TopSecret.doc"
  // E.g.: relativePath="../Sites_Secret/TopSecret.doc"
  //       documentRoot="/Users/robbie/Sites"
  //           fullPath="/Users/robbie/Sites_Secret/TopSecret"

  if (![documentRoot hasSuffix:@"/"])
  {
    documentRoot = [documentRoot stringByAppendingString:@"/"];
  }

  if (![fullPath hasPrefix:documentRoot])
  {
    return nil;
  }

  // Part 4: Search for index page if path is pointing to a directory
  if (!allowDirectory)
  {
    BOOL isDir = NO;
    if ([[NSFileManager defaultManager] fileExistsAtPath:fullPath isDirectory:&isDir] && isDir)
    {
      NSArray *indexFileNames = [self directoryIndexFileNames];

      for (NSString *indexFileName in indexFileNames)
      {
        NSString *indexFilePath = [fullPath stringByAppendingPathComponent:indexFileName];

        if ([[NSFileManager defaultManager] fileExistsAtPath:indexFilePath isDirectory:&isDir] && !isDir)
        {
          return indexFilePath;
        }
      }

      // No matching index files found in directory
      return nil;
    }
  }

  // XXX HACKHACK serve cordova.js from the containing folder
  if ([path isEqualToString:@"/cordova.js"] || [path isEqualToString:@"/cordova_plugins.js"] || [path hasPrefix:@"/plugins/"])
    return [[METEORCordovajsRoot stringByAppendingPathComponent:path] stringByStandardizingPath];

  return fullPath;
}

- (NSArray *)directoryIndexFileNames
{
  return [NSArray arrayWithObjects:@"index.html", @"index.htm", nil];
}

@end

