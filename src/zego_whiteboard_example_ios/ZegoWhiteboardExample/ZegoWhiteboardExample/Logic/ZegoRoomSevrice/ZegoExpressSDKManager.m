//
//  ZegoExpressSDKManager.m
//  ZegoWhiteboardExample
//
//  Created by Xuyang Nie on 2020/11/24.
//
#ifdef  ZegoRoomSeviceSDKFlagExpress
#import "ZegoExpressSDKManager.h"
#import <ZegoExpressEngine/ZegoExpressEngine.h>


@interface ZegoExpressSDKManager()<ZegoEventHandler>

@property (nonatomic, strong) ZegoExpressEngine *api;
@end
@implementation ZegoExpressSDKManager

+ (instancetype)shareManager {
    static ZegoExpressSDKManager *manager = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        manager = [[ZegoExpressSDKManager alloc] init];
    });
    return manager;
}

- (NSString *)getExpressVersion {
    return [ZegoExpressEngine getVersion];
}

- (void)setDelegate:(__nullable id<ZegoRoomSeviceClientDelegate>)delegate {
    _delegate = delegate;
}

- (void)initSDKWithDelegate:(nullable id<ZegoRoomSeviceClientDelegate>)delegate complementBlock:(void(^_Nullable)(NSInteger error))complementBlock{
    
    _delegate = delegate;
    BOOL result = [ZegoLocalEnvManager shareManager].roomSeviceTestEnv;
    self.api = [ZegoExpressEngine createEngineWithAppID:[ZegoLocalEnvManager shareManager].appID appSign:[ZegoLocalEnvManager shareManager].appSign isTestEnv:[ZegoLocalEnvManager shareManager].roomSeviceTestEnv scenario:2 eventHandler:self];
    if (complementBlock) {
        complementBlock(0);
    }
}


- (void)loginRoom {
    ZegoUser *user = [[ZegoUser alloc] initWithUserID:[ZegoLocalEnvManager shareManager].userName userName:[ZegoLocalEnvManager shareManager].userName];
    [self.api loginRoom:[ZegoLocalEnvManager shareManager].roomID user:user];
    
}


- (void)uploadLog {
    [self.api uploadLog];
}

- (void)writeLog:(int)logLevel
         content:(nonnull NSString *)content {
    
}

- (void)logoutRoom {
    [self.api logoutRoom:[ZegoLocalEnvManager shareManager].roomID];
}

- (void)uninit {
    [ZegoExpressEngine destroyEngine:nil];
}

- (void)onDisconnect:(int)errorCode roomID:(NSString *_Nullable)roomID {
    if ([self.delegate respondsToSelector:@selector(onDisconnect:roomID:)]) {
        [self.delegate onDisconnect:errorCode roomID:roomID];
    }
}

- (void)onReconnect:(int)errorCode roomID:(NSString *_Nullable)roomID {
    if ([self.delegate respondsToSelector:@selector(onReconnect:roomID:)]) {
        [self.delegate onReconnect:errorCode roomID:roomID];
    }
}

- (void)onTempBroken:(int)errorCode roomID:(NSString *_Nullable)roomID {
    if ([self.delegate respondsToSelector:@selector(onTempBroken:roomID:)]) {
        [self.delegate onTempBroken:errorCode roomID:roomID];
    }
}

- (void)onRoomStateUpdate:(ZegoRoomState)state errorCode:(int)errorCode extendedData:(NSDictionary *)extendedData roomID:(NSString *)roomID {
    
    if (state == ZegoRoomStateConnected && errorCode == 0) {
        if ([self.delegate respondsToSelector:@selector(onLoginRoom:)]) {
            [self.delegate onLoginRoom:0];
        }
    }
}


@end
#endif
