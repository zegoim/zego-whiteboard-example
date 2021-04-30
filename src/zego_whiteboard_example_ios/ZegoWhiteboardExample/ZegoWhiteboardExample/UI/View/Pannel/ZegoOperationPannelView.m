//
//  ZegoOperationPannelView.m
//  ZegoWhiteboardExample
//
//  Created by Xuyang Nie on 2020/11/27.
//

#import "ZegoOperationPannelView.h"
#import "ZegoSettingSwitchTableViewCell.h"
#import "ZegoSettingTextTableViewCell.h"
#import "ZegoSettingFunctionPannelTableViewCell.h"
#import "ZegoSettingPickerTableViewCell.h"
#import "ZegoOperationPannelEventHandler.h"
#import <YYModel.h>
@interface ZegoOperationPannelView()<UITableViewDelegate,UITableViewDataSource,ZegoOperationPannelEventHandlerDelegate>

@property (nonatomic, strong) NSArray *fileDataArray;
@property (nonatomic, strong) UITableView *tableView;
@property (nonatomic, strong) ZegoOperationPannelEventHandler *eventHandler;
@property (nonatomic, copy) NSString *fileName;

@end
@implementation ZegoOperationPannelView

- (instancetype)initWithFileName:(NSString *)fileName
{
    if (self = [super init]) {
        self.eventHandler = [[ZegoOperationPannelEventHandler alloc] init];
        self.eventHandler.delegate = self;
        _fileName = fileName;
        [self setupUI];
        [self addObservation];
        [self loadDataFromFile:fileName];
    }
    return self;
}

- (void)addObservation {
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(reload) name:@"reloadPlist" object:nil];
}

- (void)reload {
    [self loadDataFromFile:self.fileName];
}

- (void)setupUI {
    self.tableView = [[UITableView alloc] initWithFrame:self.bounds style:UITableViewStylePlain];
    [self addSubview:self.tableView];
    self.tableView.delegate = self;
    self.tableView.dataSource = self;
    self.tableView.separatorStyle = UITableViewCellSeparatorStyleNone;
    [self.tableView registerClass:[UITableViewCell class] forCellReuseIdentifier:@"cell"];
    [self.tableView registerClass:[ZegoSettingSwitchTableViewCell class] forCellReuseIdentifier:ZegoSettingSwitchTableViewCellID];
    [self.tableView registerClass:[ZegoSettingPickerTableViewCell class] forCellReuseIdentifier:ZegoSettingPickerTableViewCellID];
    [self.tableView registerClass:[ZegoSettingTextTableViewCell class]
           forCellReuseIdentifier:ZegoSettingTextTableViewCellID];
    [self.tableView registerClass:[ZegoSettingFunctionPannelTableViewCell class] forCellReuseIdentifier:ZegoSettingFunctionPannelTableViewCellID];
}


- (void)loadDataFromFile:(NSString *)fileName
{
    NSString *sandPath = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES).firstObject;
    NSString *filePath = [NSString stringWithFormat:@"%@/%@.plist", sandPath, fileName];

    NSArray *pannelContentArray = [NSArray arrayWithContentsOfFile:filePath];
    NSMutableArray *temp = [NSMutableArray array];
    for (NSDictionary *dic in pannelContentArray) {
        ZegoCommonCellModel *unitModel = [ZegoCommonCellModel yy_modelWithDictionary:dic];
        if (unitModel.eventNumber == ZegoOperationEventFlagTypeCustomH5Thumbnails) {
            unitModel.value = @"thumbnails/1.jpeg;thumbnails/2.jpeg;thumbnails/3.jpeg;thumbnails/4.jpeg;thumbnails/5.jpeg";
        } else if(unitModel.eventNumber == ZegoOperationEventFlagTypeStepAutoPaging) {
            unitModel.value = @1;
        }
        [temp addObject:unitModel];
    }
    self.fileDataArray = temp.copy;
    [self.tableView reloadData];
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return self.fileDataArray.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    ZegoCommonCellModel *cellModel = self.fileDataArray[indexPath.row];
    if (cellModel.type == ZegoSettingTableViewCellTypeSwitch) {
       ZegoSettingSwitchTableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:ZegoSettingSwitchTableViewCellID forIndexPath:indexPath];
        cell.model = cellModel;
        cell.delegate = self.eventHandler;
        return cell;
    } else if (cellModel.type == ZegoSettingTableViewCellTypeText){
        ZegoSettingTextTableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:ZegoSettingTextTableViewCellID forIndexPath:indexPath];
        cell.model = cellModel;
        cell.delegate = self.eventHandler;
        return cell;
    } else if (cellModel.type == ZegoSettingTableViewCellTypePicker){
        ZegoSettingPickerTableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:ZegoSettingPickerTableViewCellID forIndexPath:indexPath];
        cell.model = cellModel;
        cell.delegate = self.eventHandler;
        return cell;
    } else if (cellModel.type == ZegoSettingTableViewCellTypeFunctionPannel){
        ZegoSettingFunctionPannelTableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:ZegoSettingFunctionPannelTableViewCellID forIndexPath:indexPath];
        cell.model = cellModel;
        cell.delegate = self.eventHandler;
        return cell;
    } else {
        return [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:@"cell"];
    }
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    ZegoCommonCellModel *cellModel = self.fileDataArray[indexPath.row];
    if (cellModel.type == ZegoSettingTableViewCellTypeSwitch) {
        return [ZegoSettingSwitchTableViewCell getSwitchCellHeightWithModel:cellModel];
    } else if (cellModel.type == ZegoSettingTableViewCellTypeText){
        return [ZegoSettingTextTableViewCell getTextCellHeightWithModel:cellModel];
    } else if (cellModel.type == ZegoSettingTableViewCellTypePicker){
        return [ZegoSettingPickerTableViewCell getPickerCellHeight];
    } else if (cellModel.type == ZegoSettingTableViewCellTypeFunctionPannel){
        return [ZegoSettingFunctionPannelTableViewCell getFunctionPannelCellHeight];
    } else {
        return 0;
    }
}



- (void)layoutSubviews {
    [super layoutSubviews];
    [self.tableView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.right.top.bottom.equalTo(self);
    }];
}

- (void)dealloc {
    DLog(@" %@ dealloc",self.class);
}

#pragma mark - ZegoOperationPannelEventHandlerDelegate
- (void)pictureSelect {
    if (self.delegate && [self.delegate respondsToSelector:@selector(chousePictureEvent)]) {
        [self.delegate chousePictureEvent];
    }
}

@end
