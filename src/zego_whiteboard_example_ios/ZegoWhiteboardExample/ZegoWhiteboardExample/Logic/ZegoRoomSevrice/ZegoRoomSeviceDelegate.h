//
//  ZegoRoomSeviceDelegate.h
//  ZegoWhiteboardExample
//
//  Created by Xuyang Nie on 2020/11/24.
//

#ifndef ZegoRoomSeviceDelegate_h
#define ZegoRoomSeviceDelegate_h

//****** 房间服务事件处理协议 ******
@protocol ZegoRoomSeviceClientDelegate <NSObject>

@optional
/**
 与 server 断开通知
 
 @param errorCode 错误码，0 表示无错误
 @param roomID 房间 ID
 @discussion 建议开发者在此通知中进行重新登录、推/拉流、报错、友好性提示等其他恢复逻辑。与 server 断开连接后，SDK 会进行重试，重试失败抛出此错误。请注意，此时 SDK 与服务器的所有连接均会断开
 */
- (void)onDisconnect:(int)errorCode roomID:(NSString *_Nullable)roomID;

/**
 与 server 重连成功通知
 
 @param errorCode 错误码，0 表示无错误
 @param roomID 房间 ID
 */
- (void)onReconnect:(int)errorCode roomID:(NSString *_Nullable)roomID;

/**
 与 server 连接中断通知，SDK会尝试自动重连
 
 @param errorCode 错误码，0 表示无错误
 @param roomID 房间 ID
 */
- (void)onTempBroken:(int)errorCode roomID:(NSString *_Nullable)roomID;

- (void)onLoginRoom:(int)errorCode;
@end

//****** 房间服务统一接口协议 ******

@protocol ZegoRoomSeviceDelegate <NSObject>

@required

- (void)setDelegate:(__nullable id<ZegoRoomSeviceDelegate>)delegate;

- (void)initSDKWithDelegate:(nullable id<ZegoRoomSeviceClientDelegate>)delegate complementBlock:(void(^_Nullable)(NSInteger error))complementBlock;

- (void)loginRoom;

- (void)logoutRoom;

- (void)uploadLog;

- (void)writeLog:(int)logLevel content:(nonnull NSString *)content;

- (void)uninit;

@end

#endif /* ZegoRoomSeviceDelegate_h */
