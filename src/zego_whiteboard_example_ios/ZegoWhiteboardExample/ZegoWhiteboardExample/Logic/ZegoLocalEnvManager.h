//
//  ZegoLocalEnvManager.h
//  ZegoWhiteboardExample
//
//  Created by Xuyang Nie on 2020/11/24.
//

#import <Foundation/Foundation.h>
#define ZegoRoomSeviceTestEnv @"ZegoRoomSeviceTestEnv"
#define ZegoDocsSeviceTestEnv @"ZegoDocsSeviceTestEnv"
#define ZegoEnableCustomFont @"ZegoEnableCustomFont"
#define ZegoLoginUserName @"ZegoLoginUserName"
#define ZegoLoginRoomID @"ZegoLoginRoomID"
NS_ASSUME_NONNULL_BEGIN

@interface ZegoLocalEnvManager : NSObject
@property (nonatomic, assign, readonly) unsigned int appID;
@property (nonatomic, copy, readonly) NSString *appSign;

@property (nonatomic, copy, readonly) NSString *userName;
@property (nonatomic, copy, readonly) NSString *userID;
@property (nonatomic, copy, readonly) NSString *roomID;
@property (nonatomic, assign, readonly) BOOL roomSeviceTestEnv;
@property (nonatomic, assign, readonly) BOOL docsSeviceTestEnv;
//是否使用自定义字体，即思源字体
@property (nonatomic, assign, readonly) BOOL enableCutomFont;

+ (instancetype)shareManager;

//是否 开启房间服务SDK 测试环境
- (void)setupRoomSeviceTestEnv:(BOOL)env;
//是否 开启文件SDK 测试环境
- (void)setupDocsSeviceTestEnv:(BOOL)env;
//设置当前登录用户信息
- (void)setupCurrentUserName:(NSString *)userName roomID:(NSString *)roomID;
//是否使用自定义字体
- (void)setupEnableCustomFont:(BOOL)enable;
@end

NS_ASSUME_NONNULL_END
