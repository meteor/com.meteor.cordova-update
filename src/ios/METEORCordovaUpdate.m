/********* com.meteor.cordova-update Cordova Plugin Implementation *******/

#import <Cordova/CDV.h>

#import "METEORCordovaURLProtocol.h"

@interface METEORCordovaUpdate : CDVPlugin {
}

- (void)startServing:(CDVInvokedUrlCommand*)command;
- (void)setLocalPath:(CDVInvokedUrlCommand*)command;


@end

extern NSString *METEORDocumentRoot;
extern NSString *METEORCordovajsRoot;

@implementation METEORCordovaUpdate

- (void)pluginInitialize
{
}

- (void)startServing:(CDVInvokedUrlCommand*)command
{
  METEORDocumentRoot = [command.arguments objectAtIndex:0];
  METEORCordovajsRoot = [command.arguments objectAtIndex:1];

  [NSURLProtocol registerClass:[METEORCordovaURLProtocol class]];

  [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"meteor.local"] callbackId:command.callbackId];
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

