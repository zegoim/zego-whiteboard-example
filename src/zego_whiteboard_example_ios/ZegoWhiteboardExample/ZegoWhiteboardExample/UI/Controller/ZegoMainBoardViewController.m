//
//  ZegoMainBoardViewController.m
//  ZegoWhiteboardExample
//
//  Created by Xuyang Nie on 2020/11/26.
//

#import "ZegoMainBoardViewController.h"
#import "ZegoBoardContainerView.h"
#import "ZegoFunctionPannelView.h"
#import "ZegoTopBarView.h"
#import "ZegoFileSelectView.h"

@interface ZegoMainBoardViewController ()<ZegoBoardServiceDelegate,ZegoRoomSeviceClientDelegate,ZegoBoardContainerViewDelegate,ZegoTopBarViewDelegate,ZegoFileSelectViewDelegate>
@property (nonatomic, strong) ZegoBoardContainerView *boardContainerView;
@property (nonatomic, strong) ZegoFunctionPannelView *functionPannelView;
@property (nonatomic, strong) ZegoTopBarView *topBarView;
@property (nonatomic, strong) ZegoFileSelectView *fileSelectView;


@end

@implementation ZegoMainBoardViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    [self setupLogic];
    [self setupUI];
}

//初始化 RoomServiceManager 和 BoardServiceManager
//RoomServiceManager 主要用来做房间登录，房间状态通知
//BoardServiceManager 主要是用来设置白板和文件的属性及操作
- (void)setupLogic {
    
    [ZegoRoomSeviceCenter initSDKWithDelegate:self complementBlock:^(NSInteger error) {
        if (error == 0) {
            [[ZegoBoardServiceManager shareManager] initWithAppID:[ZegoLocalEnvManager shareManager].appID appSign:[ZegoLocalEnvManager shareManager].appSign delegate:self];
        }
    }];
}

- (void)viewWillAppear:(BOOL)animated {
    self.navigationController.navigationBar.hidden = YES;
    
}

- (void)viewWillDisappear:(BOOL)animated {
    self.navigationController.navigationBar.hidden = NO;
    
}

- (void)setupUI {
    self.view.backgroundColor = kThemeColorGray;
    
    //顶部操作视图
    self.topBarView = [[ZegoTopBarView alloc] init];
    [self.view addSubview:self.topBarView];
    self.topBarView.delegate = self;
    
    //右侧白板及文件操作视图
    self.functionPannelView = [[ZegoFunctionPannelView alloc] init];
    [self.view addSubview:self.functionPannelView];
    
    //展示画板视图
    self.boardContainerView = [[ZegoBoardContainerView alloc] init];
    [self.view addSubview:self.boardContainerView];
    self.boardContainerView.delegate = self;
    self.boardContainerView.backgroundColor = [UIColor whiteColor];
    [ZegoBoardServiceManager shareManager].boardContainnerView = self.boardContainerView;
    
    self.fileSelectView = [[ZegoFileSelectView alloc] initWithFrame:CGRectMake(0, 0, kScreenWidth, kScreenHeight)];
    self.fileSelectView.delegate = self;
}

- (void)viewWillLayoutSubviews {
    [super viewWillLayoutSubviews];
    [self.topBarView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.equalTo(self.view);
        make.right.equalTo(self.functionPannelView.mas_left);
        make.height.mas_equalTo(55);
        if (@available(iOS 11.0, *)) {
            make.left.equalTo(self.view).offset(self.view.safeAreaInsets.left);
        } else {
            make.left.equalTo(self.view);
        }
    }];
    
    [self.boardContainerView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.leading.trailing.equalTo(self.topBarView);
        make.top.equalTo(self.topBarView.mas_bottom);
        make.bottom.equalTo(self.view);
    }];
    
    [self.functionPannelView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.equalTo(self.topBarView);
        make.width.mas_equalTo(kFunctionPannelViewWidth);
        make.bottom.equalTo(self.view);
        if (@available(iOS 11.0, *)) {
            make.right.equalTo(self.view).offset(-(self.view.safeAreaInsets.right));
        } else {
            make.right.equalTo(self.view);
        }
    }];
}

#pragma mark - ZegoBoardServiceDelegate

//将本地或远端获取的白板插入 本地白板列表
- (void)insertWhiteboardIntoList:(ZegoWhiteboardView *)whiteboardView {
    NSMutableArray *temp = [NSMutableArray arrayWithArray:[ZegoBoardServiceManager shareManager].whiteboardViewList];
    [temp addObject:whiteboardView];
    [[ZegoBoardServiceManager shareManager] setupWhiteboardViewList:temp.copy];
    self.fileSelectView.fileList = [ZegoBoardServiceManager shareManager].whiteboardViewList;
    DLog(@"insertWhiteboardIntoList，whiteboardListCount：%lu",(unsigned long)temp.count);
}

- (void)onLocalInintComplementErrorCode:(NSInteger)errorCode {
    if (errorCode == 0 ) {
        //初始化完成，登录房间
        [ZegoRoomSeviceCenter loginRoom];
    }else {
        NSException *e = [NSException exceptionWithName:@"白板初始化异常" reason:[NSString stringWithFormat:@"错误码:%ld", (long)errorCode] userInfo:nil];
        [ZegoProgessHUD showTipMessage:[NSString stringWithFormat:@"白板初始化失败 %ld, 程序中止", (long)errorCode]];
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
            [e raise];
        });
    }
    DLog(@"SDK initFinish errorCode:%ld",(long)errorCode);

}

//本地白板视图创建完成回调
- (void)onLocalCreateWhiteboardView:(ZegoWhiteboardView *)whiteboardView docsView:(nonnull ZegoDocsView *)docsView errorCode:(NSInteger)errorCode {
    DLog(@"localWhiteboardAddFinish  errorCode：%ld,whiteboardID:%llu",(long)errorCode,whiteboardView.whiteboardModel.whiteboardID);
    if (errorCode == 0) {
        [self.boardContainerView addWhiteboardView:whiteboardView];
        [self insertWhiteboardIntoList:whiteboardView];
    }
}

//获取远端白板列表完成回调
- (void)onLocalGetWhiteboardList:(NSArray *)whiteboardList errorCode:(NSInteger)errorCode {
    DLog(@"getWhiteboardListFinish，errorCode:%ld whiteboardListCount:%lu",(long)errorCode,(unsigned long)whiteboardList.count);
    if (errorCode == 0) {
        if (whiteboardList.count == 0) {
            ZegoWhiteboardViewModel *model = [[ZegoWhiteboardViewModel alloc] init];
            model.aspectWidth = 16.0 * kWhiteboarPageCount;
            model.aspectHeight = 9.0;
            model.pageCount = kWhiteboarPageCount;
            model.name = [NSString stringWithFormat:@"%@的白板",[ZegoLocalEnvManager shareManager].userName];
            [[ZegoBoardServiceManager shareManager] createWhiteboardWithModel:model fileID:@""];
            
        }
        [[ZegoBoardServiceManager shareManager] setupWhiteboardViewList:whiteboardList];
    }
}

//接收远端新增白板消息
- (void)onRemoteWhiteboardAdd:(ZegoWhiteboardView *)whiteboardView {
    [self insertWhiteboardIntoList:whiteboardView];
    DLog(@"onRemoteWhiteboardAdd ,whiteboardID:%llu",whiteboardView.whiteboardModel.whiteboardID);
}

//接收远端白板移除消息
- (void)onRemoteWhiteboardRemoved:(ZegoWhiteboardID)whiteboardID {
    DLog(@"receiveRemoveWhiteboard，whiteboardID:%llu",whiteboardID);
    NSArray *whiteboardArray = [ZegoBoardServiceManager shareManager].whiteboardViewList;
    for (int i = 0; i < whiteboardArray.count; i++) {
        ZegoWhiteboardView *whiteboardView = whiteboardArray[i];
        if (whiteboardID == whiteboardView.whiteboardModel.whiteboardID) {
            [self onRemoveWhiteboardIdnex:i ];
            break;
        }
    }
}

- (void)onRemotePlayAnimation:(NSString *)animationInfo {
    [[ZegoBoardOperationManager shareManager] playAnimationWithInfo:animationInfo];
    DLog(@"onRemotePlayAnimation,animationInfo:%@",animationInfo);
}

- (void)onError:(ZegoWhiteboardViewError)error whiboardView:(nonnull ZegoWhiteboardView *)whiboardView {
    
}



#pragma mark - ZegoRoomSeviceClientDelegate

- (void)onReconnect:(int)errorCode roomID:(NSString *)roomID {
    [ZegoProgessHUD showTipMessage:@"重连成功"];
}

- (void)onTempBroken:(int)errorCode roomID:(NSString *)roomID {
    [ZegoProgessHUD showIndicatorHUDText:@"临时断开，正在重连"];
}

- (void)onDisconnect:(int)errorCode roomID:(NSString *)roomID {
    [ZegoProgessHUD showTipMessage:@"网络断开，请重新登录"];
}

- (void)onLoginRoom:(int)errorCode {
    if (errorCode == 0) {
        [[ZegoBoardServiceManager shareManager] getCurrentWhiteboardList];
    } else {
        [ZegoProgessHUD showTipMessage:@"房间登录失败，请退出重登"];
    }
}

#pragma mark - ZegoBoardContainerViewDelegate
//文件加载完成回调
- (void)onLoadFileFinish:(ZegoWhiteboardView *)whiteboardView docsView:(ZegoDocsView *)docsView currentPage:(NSInteger)currentPage {
    [self.functionPannelView reset];
    [self.topBarView setupBboardName:whiteboardView.whiteboardModel.name];
    if (docsView.pageCount) {
        [self.topBarView setupCurrentPage:currentPage totalCount:(docsView.pageCount)];
    } else if (whiteboardView.whiteboardModel.pageCount) {
        [self.topBarView setupCurrentPage:currentPage totalCount:whiteboardView.whiteboardModel.pageCount];
    } else {
        [self.topBarView setupCurrentPage:currentPage totalCount:kWhiteboarPageCount];
    }
    DLog(@"fileLoadFinish，whiteboardViewID:%llu docsViewFileName:%@",whiteboardView.whiteboardModel.whiteboardID,docsView.fileName);
}

- (void)onScrollWithCurrentPage:(NSInteger)currentPage totalPage:(NSInteger)totalPage{
    [self.topBarView setupCurrentPage:currentPage totalCount:totalPage];
    DLog(@"onScrollWithCurrentPage,currentPage:%ld ,totalPage:%d",(long)currentPage,totalPage);
}

#pragma mark - ZegoTopBarViewDelegate

- (void)onShowWhiteboardSelect {
    self.fileSelectView.fileList = [ZegoBoardServiceManager shareManager].whiteboardViewList;
    [self.fileSelectView showFileSelectView];
    DLog(@"onShowWhiteboardSelect, whiteboardListCount:%d",self.fileSelectView.fileList.count);
}

#pragma mark - ZegoFileSelectViewDelegate
//白板列表选择回调
- (void)onSelectedWhiteboardIndex:(NSInteger)index {
    NSArray *whiteboardList = [ZegoBoardServiceManager shareManager].whiteboardViewList;
    if (index < whiteboardList.count) {
        ZegoWhiteboardView *view = whiteboardList[index];
        [self.boardContainerView addWhiteboardView:view];
        DLog(@"selectWhiteboardFinish，index:%ld WhiteboardViewID:%llu",(long)index,view.whiteboardModel.whiteboardID);
    } else {
        [ZegoProgessHUD showTipMessage:@"选择白板不存在"];
        DLog(@"selectWhiteboardFinish，whiteboard not found. index:%ld WhiteboardViewListCount:%lu",(long)index,(unsigned long)whiteboardList.count);
    }
    
}

//移除本地文件
- (void)onRemoveWhiteboardIdnex:(NSInteger)index{
    //重置本地白板列表
    ZegoWhiteboardView *view = [ZegoBoardServiceManager shareManager].whiteboardViewList[index];
    NSMutableArray *temp = [NSMutableArray arrayWithArray:[ZegoBoardServiceManager shareManager].whiteboardViewList];
    [temp removeObjectAtIndex:index];
    [[ZegoBoardServiceManager shareManager] setupWhiteboardViewList:temp.copy];
    //本地白板移除
    [self.boardContainerView removeWhiteboardWithID:view.whiteboardModel.whiteboardID];
    DLog(@"LocalWhiteboardRemoveFinish, index:%ld whiteboardView：%@",(long)index,view);
    
}

@end
