#import "METEORCordovaURLProtocol.h"

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
  // TODO
}

- (void)stopLoading
{
  // No-op
}

@end

