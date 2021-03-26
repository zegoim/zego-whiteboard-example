//
//  ZegoTopBarView.m
//  ZegoWhiteboardExample
//
//  Created by Xuyang Nie on 2020/11/27.
//

#import "ZegoTopBarView.h"
#import "ZegoFileSelectView.h"
#import <IQKeyboardManager/IQKeyboardManager.h>


#define TopBar_UnitHeight 20
#define TopBar_BoardMargin 5
#define TopBar_InnerMargin 5
#define TopBar_BtnWidth 60
#define TopBar_TitleWidth 100

@interface ZegoTopBarView()<UITextFieldDelegate>
@property (nonatomic, strong) UILabel *boardNameLabel;
@property (nonatomic, strong) UILabel *roomIDLabel;
@property (nonatomic, strong) UILabel *currentPageLabel;
@property (nonatomic, strong) UILabel *totalPageLabel;
@property (nonatomic, strong) UITextField *pageInputTF;
@property (nonatomic, strong) UIButton *nextPageBtn;
@property (nonatomic, strong) UIButton *previousPageBtn;
@property (nonatomic, strong) UIButton *nextStepBtn;
@property (nonatomic, strong) UIButton *previousStepBtn;
@property (nonatomic, strong) UIButton *leaveRoomBtn;
@property (nonatomic, strong) UIButton *fileSelectBtn;

@property (nonatomic, strong) NSArray *fileListArray;


@end
@implementation ZegoTopBarView

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        [self setupUI];
    }
    return self;
}

- (void)setupUI {
    self.backgroundColor = [UIColor whiteColor];
    self.boardNameLabel = [self createLabel];
    self.roomIDLabel = [self createLabel];
    self.currentPageLabel = [self createLabel];
    self.totalPageLabel = [self createLabel];
    
    self.roomIDLabel.text = [NSString stringWithFormat:@"房间ID:%@",[ZegoLocalEnvManager shareManager].roomID];
    
    self.nextPageBtn = [self createButton];
    [self.nextPageBtn setTitle:@"下一页" forState:UIControlStateNormal];
    [self.nextPageBtn addTarget:self action:@selector(nextPage) forControlEvents:UIControlEventTouchUpInside];
    
    self.previousPageBtn = [self createButton];
    [self.previousPageBtn setTitle:@"上一页" forState:UIControlStateNormal];
    [self.previousPageBtn addTarget:self action:@selector(previousPage) forControlEvents:UIControlEventTouchUpInside];
    
    self.nextStepBtn = [self createButton];
    [self.nextStepBtn setTitle:@"下一步" forState:UIControlStateNormal];
    [self.nextStepBtn addTarget:self action:@selector(nextStep) forControlEvents:UIControlEventTouchUpInside];
    
    self.previousStepBtn = [self createButton];
    [self.previousStepBtn setTitle:@"上一步" forState:UIControlStateNormal];
    [self.previousStepBtn addTarget:self action:@selector(previousStep) forControlEvents:UIControlEventTouchUpInside];
    
    self.leaveRoomBtn = [self createButton];
    [self.leaveRoomBtn setTitle:@"离开房间" forState:UIControlStateNormal];
    [self.leaveRoomBtn addTarget:self action:@selector(leaveRoom) forControlEvents:UIControlEventTouchUpInside];
    
    self.fileSelectBtn = [self createButton];
    [self.fileSelectBtn setTitle:@"选择白板" forState:UIControlStateNormal];
    [self.fileSelectBtn addTarget:self action:@selector(didClickSelectFile:) forControlEvents:UIControlEventTouchUpInside];
    
    self.pageInputTF = [[UITextField alloc] init];
    [self addSubview:self.pageInputTF];
    self.pageInputTF.delegate = self;
    self.pageInputTF.placeholder = @"跳转页码";
    self.pageInputTF.font = [UIFont systemFontOfSize:12];
    self.pageInputTF.keyboardType = UIKeyboardTypeNumberPad;
    self.pageInputTF.enablesReturnKeyAutomatically = YES;
    
    self.boardNameLabel.text = @"白板名称:0";
    self.currentPageLabel.text = @"当前页:0";
    self.totalPageLabel.text = @"总页数:0";
}

- (BOOL)textFieldShouldBeginEditing:(UITextField *)textField {
    [IQKeyboardManager sharedManager].enableAutoToolbar = YES;
    return YES;
}

- (void)textFieldDidEndEditing:(UITextField *)textField
{
    NSInteger page = [textField.text integerValue];
    [[ZegoBoardOperationManager shareManager] turnToPage:page complementBlock:^(BOOL isScrollSuccess) {
            
    }];
    [IQKeyboardManager sharedManager].enableAutoToolbar = NO;
}



- (void)layoutSubviews {
    [super layoutSubviews];
    [self.roomIDLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.leading.top.mas_equalTo(TopBar_BoardMargin);
        make.right.equalTo(self.fileSelectBtn.mas_left).offset(-TopBar_BoardMargin);
        make.height.mas_equalTo(TopBar_UnitHeight);

    }];
    [self.boardNameLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.leading.width.height.equalTo(self.roomIDLabel);
        make.top.equalTo(self.roomIDLabel.mas_bottom).offset(TopBar_InnerMargin);
    }];
    [self.fileSelectBtn mas_makeConstraints:^(MASConstraintMaker *make) {
        make.leading.equalTo(self.roomIDLabel.mas_trailing).offset(TopBar_BoardMargin);
        make.width.mas_equalTo(TopBar_BtnWidth);
        make.top.equalTo(self).offset(TopBar_BoardMargin);
        make.bottom.equalTo(self).offset(-TopBar_BoardMargin);
    }];
    [self.previousPageBtn mas_makeConstraints:^(MASConstraintMaker *make) {
        make.leading.equalTo(self.fileSelectBtn.mas_trailing).offset(TopBar_BoardMargin);
        make.top.equalTo(self.roomIDLabel);
        make.width.mas_equalTo(TopBar_BtnWidth);
        make.height.mas_equalTo(TopBar_UnitHeight);
    }];
    [self.nextPageBtn mas_makeConstraints:^(MASConstraintMaker *make) {
        make.leading.equalTo(self.previousPageBtn.mas_trailing).offset(TopBar_BoardMargin);
        make.top.width.height.equalTo(self.previousPageBtn);
    }];
    [self.previousStepBtn mas_makeConstraints:^(MASConstraintMaker *make) {
        make.leading.equalTo(self.previousPageBtn);
        make.width.height.equalTo(self.previousPageBtn);
        make.top.equalTo(self.previousPageBtn.mas_bottom).offset(TopBar_InnerMargin);
    }];
    [self.nextStepBtn mas_makeConstraints:^(MASConstraintMaker *make) {
        make.leading.equalTo(self.nextPageBtn);
        make.width.height.equalTo(self.nextPageBtn);
        make.top.equalTo(self.nextPageBtn.mas_bottom).offset(TopBar_InnerMargin);
    }];
    [self.totalPageLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.leading.equalTo(self.nextPageBtn.mas_trailing).offset(TopBar_BoardMargin);
        make.top.equalTo(self.nextPageBtn);
        make.width.mas_equalTo(TopBar_BtnWidth);
        make.height.mas_equalTo(TopBar_UnitHeight);
    }];
    [self.currentPageLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.leading.equalTo(self.totalPageLabel);
        make.top.equalTo(self.totalPageLabel.mas_bottom).offset(TopBar_InnerMargin);
        make.width.height.equalTo(self.totalPageLabel);
    }];
    [self.pageInputTF mas_makeConstraints:^(MASConstraintMaker *make) {
        make.leading.equalTo(self.currentPageLabel.mas_trailing).offset(TopBar_BoardMargin);
        make.top.equalTo(self.totalPageLabel);
        make.width.mas_equalTo(TopBar_BtnWidth);
        make.height.mas_equalTo(TopBar_UnitHeight);
        make.trailing.equalTo(self);
    }];
    [self.leaveRoomBtn mas_makeConstraints:^(MASConstraintMaker *make) {
        make.trailing.equalTo(self);
        make.leading.equalTo(self.pageInputTF);
        make.top.equalTo(self.pageInputTF.mas_bottom).offset(TopBar_BoardMargin);
        make.width.height.equalTo(self.pageInputTF);
    }];
}

- (UILabel *)createLabel {
    UILabel *label = [[UILabel alloc] init];
    [self addSubview:label];
    label.font = [UIFont systemFontOfSize:12];
    label.backgroundColor = kThemeColorBlue;
    return label;
}

- (UIButton *)createButton {
    UIButton *btn = [UIButton buttonWithType:UIButtonTypeCustom];
    [self addSubview:btn];
    [btn setTitleColor:[UIColor blackColor] forState:UIControlStateNormal];
    btn.titleLabel.font = [UIFont systemFontOfSize:12];
    [btn setBackgroundColor:kThemeColorBlue];
    return btn;
}

- (void)setupBboardName:(NSString *)boardName {
    self.boardNameLabel.text = [NSString stringWithFormat:@"%@-%@",boardName,[ZegoLocalEnvManager shareManager].userID];
    DLog(@"***topBar setupBboardName:%@",boardName);
}

- (void)setupCurrentPage:(NSInteger)currentPage totalCount:(NSInteger)totalCount {
    
    self.currentPageLabel.text = [NSString stringWithFormat:@"当前页:%ld",(long)currentPage];
    self.totalPageLabel.text = [NSString stringWithFormat:@"总页数:%ld",(long)totalCount];
    DLog(@"***topBar setupCurrentPage:%d totalCount:%d",currentPage,totalCount);
}

- (void)didClickSelectFile:(UIButton *)sender {
    DLog(@"***topBar didClickSelectFile");
    if ([self.delegate respondsToSelector:@selector(onShowWhiteboardSelect)]) {
        [self.delegate onShowWhiteboardSelect];
    }
}

- (void)nextPage {
    [[ZegoBoardOperationManager shareManager] nextPageComplement:^(BOOL isScrollSuccess) {
        DLog(@"***topBar nextPage finish,error:%d",isScrollSuccess?1:0);
    }];
}

- (void)previousPage {
    [[ZegoBoardOperationManager shareManager] previousPageComplement:^(BOOL isScrollSuccess) {
        DLog(@"***topBar previousPage finish,error:%d",isScrollSuccess?1:0);
    }];
}

- (void)nextStep {
    [[ZegoBoardOperationManager shareManager] nextStepComplement:^(BOOL isScrollSuccess) {
        DLog(@"***topBar nextStep finish,error:%d",isScrollSuccess?1:0);
    }];
}

- (void)previousStep {
    [[ZegoBoardOperationManager shareManager] previousStepComplement:^(BOOL isScrollSuccess) {
        DLog(@"***topBar previousStep finish,error:%d",isScrollSuccess?1:0);
    }];
}

- (void)turnToPage:(NSInteger)pageCount {
    [[ZegoBoardOperationManager shareManager] turnToPage:pageCount complementBlock:^(BOOL isScrollSuccess) {
        DLog(@"***topBar turnToPage:%ld finish,error:%d",(long)pageCount,isScrollSuccess?1:0);
    }];
}

- (void)leaveRoom {
    [[ZegoBoardOperationManager shareManager] leaveRoom];
    DLog(@"***topBar leaveRoom");
}

- (void)dealloc {
    DLog(@" %@ dealloc",self.class);
}
@end
