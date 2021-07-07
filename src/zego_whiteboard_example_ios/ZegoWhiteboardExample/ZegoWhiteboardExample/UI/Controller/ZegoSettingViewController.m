//
//  ZegoSettingViewController.m
//  ZegoWhiteboardExample
//
//  Created by Xuyang Nie on 2020/11/26.
//

#import "ZegoSettingViewController.h"
#import "ZegoSettingSwitchTableViewCell.h"
#import "ZegoSettingPickerTableViewCell.h"
#import "ZegoCommonCellModel.h"


@interface ZegoSettingViewController ()<UITableViewDelegate,UITableViewDataSource,ZegoSettingTableViewCellDelegate>
@property (nonatomic, strong) UITableView *settingTableView;
@property (nonatomic, strong) NSArray *dataArray;


@end

@implementation ZegoSettingViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    
    [self setupUI];
    [self setupDefaultData];
}

- (void)setupUI {
    self.view.backgroundColor = [UIColor whiteColor];
    self.settingTableView = [[UITableView alloc] initWithFrame:self.view.bounds style:UITableViewStyleGrouped];
    [self.view addSubview:self.settingTableView];
    self.settingTableView.delegate = self;
    self.settingTableView.dataSource = self;
    [self.settingTableView registerClass:[ZegoSettingSwitchTableViewCell class] forCellReuseIdentifier:ZegoSettingSwitchTableViewCellID];
    [self.settingTableView registerClass:[ZegoSettingPickerTableViewCell class] forCellReuseIdentifier:ZegoSettingPickerTableViewCellID];
}

- (void)setupDefaultData {
    NSMutableArray *dataArray = [NSMutableArray array];
    ZegoCommonCellModel *roomServiceModel = [[ZegoCommonCellModel alloc] initWithTitle:@"开启房间服务测试环境" type:ZegoSettingTableViewCellTypeSwitch options:nil value:[NSNumber numberWithBool:[ZegoLocalEnvManager shareManager].roomSeviceTestEnv]];
    roomServiceModel.tag = ZegoRoomSeviceTestEnv;
    ZegoCommonCellModel *docsServiceModel = [[ZegoCommonCellModel alloc] initWithTitle:@"开启文件服务测试环境" type:ZegoSettingTableViewCellTypeSwitch options:nil value:[NSNumber numberWithBool:[ZegoLocalEnvManager shareManager].docsSeviceTestEnv]];
    docsServiceModel.tag = ZegoDocsSeviceTestEnv;
    ZegoCommonCellModel *customFontModel = [[ZegoCommonCellModel alloc] initWithTitle:@"开启思源字体" type:ZegoSettingTableViewCellTypeSwitch options:nil value:[NSNumber numberWithBool:[ZegoLocalEnvManager shareManager].enableCutomFont]];
    customFontModel.tag = ZegoEnableCustomFont;
    ZegoCommonCellModel *pptThumbnailClarityModel = [[ZegoCommonCellModel alloc] initWithTitle:@"缩略图清晰度" type:ZegoSettingTableViewCellTypePicker options:nil value:[NSNumber numberWithBool:[ZegoLocalEnvManager shareManager].enableCutomFont]];
    ZegoCommonCellModel *docsAlphaServiceModel = [[ZegoCommonCellModel alloc] initWithTitle:@"开启文件服务alpha环境" type:ZegoSettingTableViewCellTypeSwitch options:nil value:[NSNumber numberWithBool:[ZegoLocalEnvManager shareManager].docsSeviceAlphaEnv]];
    docsAlphaServiceModel.tag = ZegoDocsSeviceAlphaEnv;
    
    ZegoCellOptionModel *ordinaryModel = [[ZegoCellOptionModel alloc]init];
    ordinaryModel.title = @"普通";
    ordinaryModel.value = @"1";
    
    ZegoCellOptionModel *highModel = [[ZegoCellOptionModel alloc]init];
    highModel.title = @"标清";
    highModel.value = @"2";
    
    ZegoCellOptionModel *superModel = [[ZegoCellOptionModel alloc]init];
    superModel.title = @"高清";
    superModel.value = @"3";
    NSArray *optionArr = @[ordinaryModel,highModel,superModel];
    pptThumbnailClarityModel.tag = ZegoPPTThumbnailClarity;
    pptThumbnailClarityModel.options = optionArr;
    
    NSDictionary *envSettingDic = @{
        @"title":@"环境设置",
        @"value":@[roomServiceModel,docsServiceModel,customFontModel,docsAlphaServiceModel,pptThumbnailClarityModel]
    };
    [dataArray addObject:envSettingDic];
    self.dataArray = dataArray.copy;
    [self.settingTableView reloadData];
    
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return self.dataArray.count;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    NSDictionary *sectionData = self.dataArray[section];
    NSArray *sectionContentArray = sectionData[@"value"];
    return sectionContentArray.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    ZegoCommonCellModel *cellModel = [self getCellModelForIndexPath:indexPath];
    ZegoSettingSwitchTableViewCell *cell;
    if (cellModel.type == ZegoSettingTableViewCellTypeSwitch) {
        cell = [tableView dequeueReusableCellWithIdentifier:ZegoSettingSwitchTableViewCellID forIndexPath:indexPath];
    } else if (cellModel.type == ZegoSettingTableViewCellTypePicker) {
        cell = [tableView dequeueReusableCellWithIdentifier:ZegoSettingPickerTableViewCellID forIndexPath:indexPath];
    }
    cell.delegate = self;
    cell.model = cellModel;
    
    return cell;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(nonnull NSIndexPath *)indexPath
{
    ZegoCommonCellModel *cellModel = [self getCellModelForIndexPath:indexPath];
    if (cellModel.type == ZegoSettingTableViewCellTypeSwitch) {
        return [ZegoSettingSwitchTableViewCell getSwitchCellHeightWithModel:cellModel];
    } else if (cellModel.type == ZegoSettingTableViewCellTypePicker) {
        return [ZegoSettingPickerTableViewCell getPickerCellHeight];
    }
    return 0;
}

- (ZegoCommonCellModel *)getCellModelForIndexPath:(NSIndexPath *)indexPath
{
    NSDictionary *sectionModel = self.dataArray[indexPath.section];
    NSArray *sectionValueArray = sectionModel[@"value"];
    ZegoCommonCellModel *cellModel = sectionValueArray[indexPath.row];
    return cellModel;
}

- (void)viewWillLayoutSubviews {
    [super viewWillLayoutSubviews];
    self.settingTableView.frame = self.view.bounds;
}

- (void)onSettingCellValueChange:(ZegoCommonCellModel *)valueChangeModel {
    if ([valueChangeModel.tag isEqualToString:ZegoRoomSeviceTestEnv]) {
        [[ZegoLocalEnvManager shareManager] setupRoomSeviceTestEnv:[valueChangeModel.value boolValue]];
    } else if ([valueChangeModel.tag isEqualToString:ZegoDocsSeviceTestEnv]) {
        [[ZegoLocalEnvManager shareManager] setupDocsSeviceTestEnv:[valueChangeModel.value boolValue]];
    } else if ([valueChangeModel.tag isEqualToString:ZegoEnableCustomFont]) {
        [[ZegoLocalEnvManager shareManager] setupEnableCustomFont:[valueChangeModel.value boolValue]];
    } else if ([valueChangeModel.tag isEqualToString:ZegoPPTThumbnailClarity]) {
        NSInteger value = [valueChangeModel.value integerValue] + 1;
        [[ZegoLocalEnvManager shareManager] setupThumbnailClarity:[NSString stringWithFormat:@"%ld",value]];
    } else if ([valueChangeModel.tag isEqualToString:ZegoDocsSeviceAlphaEnv]) {
        [[ZegoLocalEnvManager shareManager] setupDocsSeviceAlphaEnv:[valueChangeModel.value boolValue]];
    }
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
