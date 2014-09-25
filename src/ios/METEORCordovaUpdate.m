/********* com.meteor.cordova-update Cordova Plugin Implementation *******/

#import <Cordova/CDV.h>
#import "DDLog.h"
#import "DDTTYLogger.h"
#import "METEORCordovaURLProtocol.h"

@interface METEORCordovaUpdate : CDVPlugin {
}

//- (void)getCordovajsRoot:(CDVInvokedUrlCommand*)command;

@end

@implementation MeteorCordovaUpdate

-(id)init {
  if (self = [super init]) {
    [NSURLProtocol registerClass:[RNCachingURLProtocol class]];
  }

  return self;
}

@end

