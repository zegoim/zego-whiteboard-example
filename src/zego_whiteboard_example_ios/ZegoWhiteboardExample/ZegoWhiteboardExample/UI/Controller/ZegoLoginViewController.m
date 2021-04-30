//
//  ZegoLoginViewController.m
//  ZegoWhiteboardExample
//
//  Created by Xuyang Nie on 2020/11/26.
//

#import "ZegoLoginViewController.h"
#import "ZegoMainBoardViewController.h"
#import "ZegoSettingViewController.h"
@interface ZegoLoginViewController ()
@property (nonatomic, strong) UITextField *userNameTF;
@property (nonatomic, strong) UITextField *roomIDTF;
@property (nonatomic, strong) UIButton *settingBtn;
@property (nonatomic, strong) UIButton *loginBtn;

@property (nonatomic, strong) UILabel *versionLabel;
@property (nonatomic, strong) UILabel *whiteboardVersionLabel;
@property (nonatomic, strong) UILabel *whiteboardMatchVersionLabel;
@property (nonatomic, strong) UILabel *docsViewVersionLabel;
@property (nonatomic, strong) UILabel *liveRoomVersionLabel;

@end

@implementation ZegoLoginViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    [self checkReachability];   //第一次安装 App 强制弹窗提示网络连接, 否则 SDK 初始化失败
    [self setupUI];
}

- (void)checkReachability {
    UIWebView *webview = [[UIWebView alloc] initWithFrame:CGRectZero];
    NSURL *url = [[NSURL alloc] initWithString:@"https://www.zego.im/"];
    NSURLRequest *request = [[NSURLRequest alloc] initWithURL:url];
    [webview loadRequest:request];
}

- (void)setupUI {
    self.view.backgroundColor = [UIColor whiteColor];
    
    self.userNameTF = [[UITextField alloc] init];
    [self.view addSubview:self.userNameTF];
    self.userNameTF.text = [ZegoLocalEnvManager shareManager].userName;
    self.userNameTF.placeholder = @"userName";
    self.userNameTF.borderStyle = UITextBorderStyleRoundedRect;
    
    self.roomIDTF = [[UITextField alloc] init];
    [self.view addSubview:self.roomIDTF];
    self.roomIDTF.text = [ZegoLocalEnvManager shareManager].roomID;
    self.roomIDTF.placeholder = @"roomID";
    self.roomIDTF.borderStyle = UITextBorderStyleRoundedRect;
    
    self.settingBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    [self.settingBtn setTitle:@"设置" forState:UIControlStateNormal];
    [self.view addSubview:self.settingBtn];
    [self.settingBtn setTitleColor:[UIColor blueColor] forState:UIControlStateNormal];
    [self.settingBtn addTarget:self action:@selector(didClickSettingBtn:) forControlEvents:UIControlEventTouchUpInside];
    [self.navigationItem setRightBarButtonItem:[[UIBarButtonItem alloc] initWithCustomView:self.settingBtn]];
    
    self.loginBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    [self.loginBtn setTitle:@"登录" forState:UIControlStateNormal];
    [self.view addSubview:self.loginBtn];
    [self.loginBtn setTitleColor:[UIColor blueColor] forState:UIControlStateNormal];
    [self.loginBtn addTarget:self action:@selector(didClickLoginBtn:) forControlEvents:UIControlEventTouchUpInside];
    
    
    // 各种版本号
    UIColor *versionTextColor = [UIColor systemGrayColor];
    
    self.versionLabel = [[UILabel alloc] init];
    [self.view addSubview:self.versionLabel];
    self.versionLabel.textColor = versionTextColor;
    [self.versionLabel setFont:[UIFont systemFontOfSize:10]];
    NSDictionary *info = [[NSBundle mainBundle] infoDictionary];
    NSString *appVersion = [info objectForKey:@"CFBundleShortVersionString"];
    NSString *appBuildVersion = [info objectForKey:@"CFBundleVersion"];
    NSString *versionInfo = [NSString stringWithFormat:@"Demo版本: %@.%@",appVersion,appBuildVersion];
    self.versionLabel.text = versionInfo;
    
    self.whiteboardVersionLabel = [[UILabel alloc] init];
    [self.view addSubview:self.whiteboardVersionLabel];
    self.whiteboardVersionLabel.textColor = versionTextColor;
    self.whiteboardVersionLabel.text = [NSString stringWithFormat:@"白板版本: %@",[[ZegoWhiteboardManager sharedInstance] getVersion]];
    [self.whiteboardVersionLabel setFont:[UIFont systemFontOfSize:10]];
    
    self.whiteboardMatchVersionLabel = [[UILabel alloc] init];
    [self.view addSubview:self.whiteboardMatchVersionLabel];
    self.whiteboardMatchVersionLabel.textColor = versionTextColor;
    self.whiteboardMatchVersionLabel.text = [NSString stringWithFormat:@"白板校验版本: %@", ZEGO_WHITEBOARD_MATCH_VERSION];
    [self.whiteboardMatchVersionLabel setFont:[UIFont systemFontOfSize:10]];
    
    self.docsViewVersionLabel = [[UILabel alloc] init];
    [self.view addSubview:self.docsViewVersionLabel];
    self.docsViewVersionLabel.textColor = versionTextColor;
    self.docsViewVersionLabel.text = [NSString stringWithFormat:@"文件版本: %@", [[ZegoDocsViewManager sharedInstance] getVersion]];
    [self.docsViewVersionLabel setFont:[UIFont systemFontOfSize:10]];
        
    self.liveRoomVersionLabel = [[UILabel alloc] init];
    [self.view addSubview:self.liveRoomVersionLabel];
    self.liveRoomVersionLabel.textColor = versionTextColor;
#ifdef  ZegoRoomSeviceSDKFlagLiveRoom
    self.liveRoomVersionLabel.text = [NSString stringWithFormat:@"LiveRoom 版本: %@", [[ZegoLiveRoomSDKManager shareManager] getLiveRoomVersion]];
#else
    self.liveRoomVersionLabel.text = [NSString stringWithFormat:@"Express 版本: %@", [[ZegoExpressSDKManager shareManager] getExpressVersion]];
#endif
    [self.liveRoomVersionLabel setFont:[UIFont systemFontOfSize:10]];
    
}

- (void)didClickSettingBtn:(UIButton *)sender {
    ZegoSettingViewController *vc = [[ZegoSettingViewController alloc] init];
    [self.navigationController pushViewController:vc animated:YES];
}

- (void)didClickLoginBtn:(UIButton *)sender {
    [[ZegoLocalEnvManager shareManager] setupCurrentUserName:self.userNameTF.text roomID:self.roomIDTF.text];
    ZegoMainBoardViewController *vc = [[ZegoMainBoardViewController alloc] init];
    [self.navigationController pushViewController:vc animated:YES];
}

- (void)viewWillLayoutSubviews {
    [super viewWillLayoutSubviews];
    
    [self.userNameTF mas_makeConstraints:^(MASConstraintMaker *make) {
        make.width.mas_equalTo(200);
        make.height.mas_equalTo(40);
        make.centerX.equalTo(self.view);
        make.centerY.equalTo(self.view).offset(-45);
    }];
    
    [self.roomIDTF mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.equalTo(self.userNameTF.mas_bottom).offset(10);
        make.width.height.equalTo(self.userNameTF);
        make.leading.equalTo(self.userNameTF.mas_leading);
    }];
    
    [self.loginBtn mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.equalTo(self.roomIDTF.mas_bottom).offset(10);
        make.width.height.mas_equalTo(44);
        make.centerX.equalTo(self.view);
    }];
    
    // versions
    [self.versionLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.width.height.equalTo(self.docsViewVersionLabel);
        make.bottom.equalTo(self.whiteboardVersionLabel.mas_top).offset(-5);
    }];
    
    [self.whiteboardVersionLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.width.height.equalTo(self.docsViewVersionLabel);
        make.bottom.equalTo(self.whiteboardMatchVersionLabel.mas_top).offset(-5);
    }];
    
    [self.whiteboardMatchVersionLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.bottom.equalTo(self.docsViewVersionLabel.mas_top).offset(-5);
        make.left.width.height.equalTo(self.docsViewVersionLabel);
    }];
    
    [self.docsViewVersionLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.bottom.equalTo(self.liveRoomVersionLabel.mas_top).offset(-5);
        make.left.width.height.equalTo(self.liveRoomVersionLabel);
    }];
    
    [self.liveRoomVersionLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.equalTo(self.view).offset(10);
        make.bottom.equalTo(self.view).offset(-15);
        make.height.equalTo(@15);
        make.width.equalTo(self.view);
    }];
}
/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
