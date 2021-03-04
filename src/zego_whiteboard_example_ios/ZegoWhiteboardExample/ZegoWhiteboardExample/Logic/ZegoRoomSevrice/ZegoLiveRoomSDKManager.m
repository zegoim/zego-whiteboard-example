//
//  ZegoLiveRoomSDKManager.m
//  ZegoWhiteboardExample
//
//  Created by Xuyang Nie on 2020/11/24.
//
#ifdef  ZegoRoomSeviceSDKFlagLiveRoom
#import "ZegoLiveRoomSDKManager.h"
#import <ZegoLiveRoom/ZegoLiveRoomApi.h>
//#import <ZegoLiveRoom/ZegoLiveRoomApi-ReliableMessage.h>
#import <ZegoLiveRoom/ZegoLiveRoomApiDefines.h>
#import <ZegoLiveRoom/zego-api-logger-oc.h>
#import "ZGAppSignHelper.h"
@interface ZegoLiveRoomSDKManager()<ZegoRoomDelegate>

@property (nonatomic, strong) ZegoLiveRoomApi *api;
@end
@implementation ZegoLiveRoomSDKManager

+ (instancetype)shareManager {
    static ZegoLiveRoomSDKManager *manager = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        manager = [[ZegoLiveRoomSDKManager alloc] init];
    });
    return manager;
}

- (NSString *)getLiveRoomVersion {
    return [ZegoLiveRoomApi version];
}

- (void)setDelegate:(__nullable id<ZegoRoomSeviceClientDelegate>)delegate {
    _delegate = delegate;
}

- (void)initSDKWithDelegate:(nullable id<ZegoRoomSeviceClientDelegate>)delegate complementBlock:(void (^ _Nullable)(NSInteger))complementBlock{
    
    _delegate = delegate;
    BOOL result = [ZegoLocalEnvManager shareManager].roomSeviceTestEnv;
    [ZegoLiveRoomApi setUseTestEnv:result];
    [ZegoLiveRoomApi setConfig:@"room_retry_time=300"];
    [ZegoLiveRoomApi setConfig:@"preview_clear_last_frame=true"];
    [ZegoLiveRoomApi setConfig:@"play_clear_last_frame=true"];
    [ZegoLiveRoomApi setLogDir:kZegoLogPath size:10*1024*1024 subFolder:nil];
    [ZegoLiveRoomApi setUserID:[ZegoLocalEnvManager shareManager].userName userName:[ZegoLocalEnvManager shareManager].userName];
    unsigned int appID = [ZegoLocalEnvManager shareManager].appID;
    NSData *appSign = [ZGAppSignHelper converAppSignStringToData:[ZegoLocalEnvManager shareManager].appSign];
    self.api = [[ZegoLiveRoomApi alloc] initWithAppID:appID appSignature:appSign  completionBlock:^(int errorCode) {

        //WhiteBoardView 初始化
        ZegoWhiteboardConfig *configw = [[ZegoWhiteboardConfig alloc] init];
        configw.logPath = kZegoLogPath;
        [[ZegoWhiteboardManager sharedInstance] setConfig:configw];
        if (complementBlock) {
            complementBlock(errorCode);
        }
    }];
    [ self.api setRoomMaxUserCount:10];
    [ self.api setRoomConfig:YES userStateUpdate:YES];
    [ self.api setRoomDelegate:self];
//    [ self.api setReliableMessageDelegate:self];
}


- (void)loginRoom {
    __weak typeof(self) weakSelf = self;
    [self.api loginRoom:[ZegoLocalEnvManager shareManager].roomID role:1 withCompletionBlock:^(int errorCode, NSArray<ZegoStream *> *streamList) {
        __strong typeof(weakSelf) strongSelf = weakSelf;
        if ([strongSelf.delegate respondsToSelector:@selector(onLoginRoom:)]) {
            [strongSelf.delegate onLoginRoom:errorCode];
        }
    }];
}


- (void)uploadLog {
    [ZegoLiveRoomApi uploadLog];
}

- (void)writeLog:(int)logLevel
         content:(nonnull NSString *)content {
    
}

- (void)logoutRoom {
    [self.api logoutRoom];
}

- (void)uninit {
    [self logoutRoom];
    self.api = nil;
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

@end
#endif
