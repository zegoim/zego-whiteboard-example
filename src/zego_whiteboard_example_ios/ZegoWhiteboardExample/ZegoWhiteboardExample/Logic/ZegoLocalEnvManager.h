//
//  ZegoLocalEnvManager.h
//  ZegoWhiteboardExample
//
//  Created by Xuyang Nie on 2020/11/24.
//

#import <Foundation/Foundation.h>
#define ZegoRoomSeviceTestEnv @"ZegoRoomSeviceTestEnv"
#define ZegoDocsSeviceTestEnv @"ZegoDocsSeviceTestEnv"
#define ZegoDocsSeviceAlphaEnv @"ZegoDocsSeviceAlphaEnv"
#define ZegoEnableCustomFont @"ZegoEnableCustomFont"
#define ZegoPPTThumbnailClarity @"ZegoPPTThumbnailClarity"
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
@property (nonatomic, assign, readonly) BOOL docsSeviceAlphaEnv;
//是否使用自定义字体，即思源字体
@property (nonatomic, assign, readonly) BOOL enableCutomFont;

@property (nonatomic, copy, readonly) NSString *pptThumbnailClarity;

+ (instancetype)shareManager;

//是否 开启房间服务SDK 测试环境
- (void)setupRoomSeviceTestEnv:(BOOL)env;
//是否 开启文件SDK 测试环境
- (void)setupDocsSeviceTestEnv:(BOOL)env;
//是否使用 文件SDK alpha环境 如果使用alpha环境设置 设置文件测试和正式环境无效
- (void)setupDocsSeviceAlphaEnv:(BOOL)env;
//设置当前登录用户信息
- (void)setupCurrentUserName:(NSString *)userName roomID:(NSString *)roomID;
//是否使用自定义字体
- (void)setupEnableCustomFont:(BOOL)enable;
//设置上传文件缩略图的清晰度
- (void)setupThumbnailClarity:(NSString *)clarityValue;
@end

NS_ASSUME_NONNULL_END
