//
//  ZegoFunctionView.m
//  ZegoWhiteboardExample
//
//  Created by Xuyang Nie on 2020/11/27.
//

#import "ZegoFunctionPannelView.h"
#import "ZegoOperationPannelModel.h"
#import "ZegoOperationPannelView.h"
#import "ZegoCommonCellModel.h"
#import <YYModel.h>
@interface ZegoFunctionPannelView()<UIScrollViewDelegate>
@property (nonatomic, strong) UISegmentedControl *pannelSC;
@property (nonatomic, strong) UIScrollView *scrollView;
@property (nonatomic, strong) UIView *scrollContentView;

@property (nonatomic, strong) NSArray *pannelDataArray;
@property (nonatomic, strong) NSArray *pannelViewArray;

@property (nonatomic, assign) NSUInteger pannelIdx;
@end
@implementation ZegoFunctionPannelView
- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        [self copyBundlePlistFilesToSandbox];
        [self addObservation];
        [self loadDefaultData];
        [self setupUI];
    }
    return self;
}

- (void)addObservation {
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(loadDefaultData) name:@"reloadPlist" object:nil];
}

- (void)copyBundlePlistFilesToSandbox {
    NSString *sandPath = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES).firstObject;
    
    NSString *dstPath1 = [NSString stringWithFormat:@"%@/%@", sandPath, @"ZegoOperationPannel.plist"];
    NSString *dstPath2 = [NSString stringWithFormat:@"%@/%@", sandPath, @"ZegoWhiteboardPannel.plist"];
    NSString *dstPath3 = [NSString stringWithFormat:@"%@/%@", sandPath, @"ZegoDrawPannel.plist"];
    NSString *dstPath4 = [NSString stringWithFormat:@"%@/%@", sandPath, @"ZegoDocsPannel.plist"];
    NSString *dstPath5 = [NSString stringWithFormat:@"%@/%@", sandPath, @"ZegoSettingPage.plist"];
    //先将之前的清除
    [[NSFileManager defaultManager] removeItemAtPath:dstPath1 error:NULL];
    [[NSFileManager defaultManager] removeItemAtPath:dstPath2 error:NULL];
    [[NSFileManager defaultManager] removeItemAtPath:dstPath3 error:NULL];
    [[NSFileManager defaultManager] removeItemAtPath:dstPath4 error:NULL];
    [[NSFileManager defaultManager] removeItemAtPath:dstPath5 error:NULL];
    
    //将 bundle 的 plist 文件复制到沙盒
    //从bundle中获取 显示结构  title为显示名 content 为operationPannelView 需要读取的数据文件名
    //复制到沙盒
    
    NSString *filePath1 = [[NSBundle mainBundle] pathForResource:@"ZegoOperationPannel" ofType:@".plist"];
    NSString *filePath2 = [[NSBundle mainBundle] pathForResource:@"ZegoWhiteboardPannel" ofType:@".plist"];
    NSString *filePath3 = [[NSBundle mainBundle] pathForResource:@"ZegoDrawPannel" ofType:@".plist"];
    NSString *filePath4 = [[NSBundle mainBundle] pathForResource:@"ZegoDocsPannel" ofType:@".plist"];
    NSString *filePath5 = [[NSBundle mainBundle] pathForResource:@"ZegoSettingPage" ofType:@".plist"];
    
    
    
    
    [[NSFileManager defaultManager] copyItemAtPath:filePath1 toPath:dstPath1 error:NULL];
    [[NSFileManager defaultManager] copyItemAtPath:filePath2 toPath:dstPath2 error:NULL];
    [[NSFileManager defaultManager] copyItemAtPath:filePath3 toPath:dstPath3 error:NULL];
    [[NSFileManager defaultManager] copyItemAtPath:filePath4 toPath:dstPath4 error:NULL];
    [[NSFileManager defaultManager] copyItemAtPath:filePath5 toPath:dstPath5 error:NULL];
}

- (void)loadDefaultData {
    //从沙盒获取 plist 文件
    NSString *sandPath = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES).firstObject;
    NSString *filePath = [NSString stringWithFormat:@"%@/%@", sandPath, @"ZegoOperationPannel.plist"];
    self.pannelDataArray = [NSArray arrayWithContentsOfFile:filePath];
    NSMutableArray *temp = [NSMutableArray array];
    for (NSDictionary *dic in self.pannelDataArray) {
        ZegoOperationPannelModel *model = [ZegoOperationPannelModel yy_modelWithDictionary:dic];
        [temp addObject:model];
    }
    self.pannelDataArray = temp.copy;
}

- (void)setupUI {

    [self setupPannelSegment];
    
    [self setupOperationPannel];
}

- (void)setupPannelSegment {
    NSMutableArray *temp = [NSMutableArray array];
    for (ZegoOperationPannelModel *model in self.pannelDataArray) {
        [temp addObject:model.title];
    }
    self.pannelSC = [[UISegmentedControl alloc] initWithItems:temp.copy];
    [self.pannelSC addTarget:self action:@selector(didClickPannelSegment:) forControlEvents:UIControlEventValueChanged];
    [self addSubview:self.pannelSC];
    self.pannelSC.selectedSegmentIndex = 0;
    self.pannelSC.tintColor = kThemeColorBlue;
    self.pannelSC.backgroundColor = [UIColor whiteColor];

}

- (void)setupOperationPannel{
    self.scrollView = [[UIScrollView alloc] init];
    [self addSubview:self.scrollView];
    self.scrollView.delegate = self;
    self.scrollView.scrollEnabled = NO;
    
    self.scrollContentView = [[UIView alloc] init];
    [self.scrollView addSubview:self.scrollContentView];
    NSMutableArray *temp = [NSMutableArray array];
    for (ZegoOperationPannelModel *model in self.pannelDataArray) {
        //根据 content 包含的文件名从 bundle 读取页面模板
        ZegoOperationPannelView *pannelView = [[ZegoOperationPannelView alloc] initWithFileName:model.content];
        [self.scrollContentView addSubview:pannelView];
        [temp addObject:pannelView];
    }
    self.pannelViewArray = temp.copy;
    
}
 
- (void)reset {
    for (ZegoOperationPannelView *view in self.pannelViewArray) {
        [view removeFromSuperview];
    }
    self.pannelViewArray = nil;
    [self.scrollView removeFromSuperview];
    [self.pannelSC removeFromSuperview];
    self.pannelSC = nil;
    [self loadDefaultData];
    [self setupUI];
}

- (void)didClickPannelSegment:(UISegmentedControl *)sender {
    [self setPannelIdx:sender.selectedSegmentIndex];
}

- (void)setPannelIdx:(NSUInteger)pannelIdx {
    _pannelIdx = pannelIdx;
    CGFloat screenWidth = self.scrollView.bounds.size.width;
    self.scrollView.contentOffset = CGPointMake(screenWidth * pannelIdx, 0);
}

- (void)layoutSubviews {
    [super layoutSubviews];
    [self.pannelSC mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.leading.trailing.equalTo(self);
        make.height.mas_equalTo(40);
    }];
    [self.scrollView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.equalTo(self.pannelSC.mas_bottom);
        make.leading.trailing.equalTo(self.pannelSC);
        make.bottom.equalTo(self);
    }];
    [self.scrollContentView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.leading.height.equalTo(self.scrollView);
        make.width.mas_equalTo(kFunctionPannelViewWidth * self.pannelDataArray.count);
    }];
    ZegoOperationPannelView *previousView = nil;
    for (int i = 0; i < self.pannelViewArray.count; i++) {
        ZegoOperationPannelView *view = self.pannelViewArray[i];
        [view mas_makeConstraints:^(MASConstraintMaker *make) {
            make.top.and.bottom.equalTo(self.scrollContentView);
            make.width.mas_equalTo(kFunctionPannelViewWidth);
            if (previousView) {
                make.left.mas_equalTo(previousView.mas_right);
            }
            else {
                make.left.mas_equalTo(0);
            }
        }];
        previousView = view;
    }
    self.pannelIdx = self.pannelIdx;
}

- (void)dealloc {
    DLog(@" %@ dealloc",self.class);
}


@end
