/********* com.meteor.cordova-update Cordova Plugin Implementation *******/

#import <Cordova/CDV.h>

#import "METEORCordovaURLProtocol.h"

@interface CordovaUpdate : CDVPlugin {
}

- (void)startServer:(CDVInvokedUrlCommand*)command;
- (void)setLocalPath:(CDVInvokedUrlCommand*)command;


@end

extern NSString *METEORDocumentRoot;
extern NSString *METEORCordovajsRoot;

@implementation CordovaUpdate

- (void)pluginInitialize
{
}

- (void)startServer:(CDVInvokedUrlCommand*)command
{
  NSLog(@"start serving %@", command.arguments);
  METEORDocumentRoot = [command.arguments objectAtIndex:0];
  METEORCordovajsRoot = [command.arguments objectAtIndex:1];

  [NSURLProtocol registerClass:[METEORCordovaURLProtocol class]];

  [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"http://meteor.local"] callbackId:command.callbackId];
}

- (void)setLocalPath:(CDVInvokedUrlCommand*)command
{
  METEORDocumentRoot = [command.arguments objectAtIndex:0];
  [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:nil] callbackId:command.callbackId];
}

- (void)getCordovajsRoot:(CDVInvokedUrlCommand*)command
{
  [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:METEORCordovajsRoot] callbackId:command.callbackId];
}

@end

