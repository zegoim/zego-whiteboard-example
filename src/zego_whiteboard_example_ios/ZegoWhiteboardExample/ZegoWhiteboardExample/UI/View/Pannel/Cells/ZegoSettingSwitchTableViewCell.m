//
//  ZegoSettingSwitchTableViewCell.m
//  ZegoWhiteboardExample
//
//  Created by Xuyang Nie on 2020/11/26.
//

#import "ZegoSettingSwitchTableViewCell.h"


@interface ZegoSettingSwitchTableViewCell()
@property (nonatomic, strong) UILabel *titleLable;
@property (nonatomic, strong) UIButton *confirmBtn;
@property (nonatomic, strong) NSMutableArray *switchViewArray;
@property (nonatomic, strong) UIView *lineView;
@end
@implementation ZegoSettingSwitchTableViewCell

- (instancetype)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    if (self = [super initWithStyle:style reuseIdentifier:reuseIdentifier]) {
        [self setupUI];
    }
    return self;
}

- (void)prepareForReuse
{
    [super prepareForReuse];
    self.titleLable.text = nil;
    for (ZegoSettingSwitchView *sv in self.switchViewArray) {
        [sv removeFromSuperview];
    }
    [self.switchViewArray removeAllObjects];
}

- (void)setupUI {
    self.selectionStyle = UITableViewCellSelectionStyleNone;
    self.titleLable = [[UILabel alloc] init];
    [self.contentView addSubview: self.titleLable];
    self.titleLable.textColor = kTextColor1;
    self.titleLable.font = kFontText14;
    self.titleLable.hidden = YES;
    
    self.confirmBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    [self.contentView addSubview:self.confirmBtn];
    self.confirmBtn.titleLabel.font = [UIFont systemFontOfSize:14];
    [self.confirmBtn setTitle:@"确定" forState:UIControlStateNormal];
    [self.confirmBtn setTitleColor:kThemeColorPink forState:UIControlStateNormal];
    [self.confirmBtn setBackgroundImage:[UIImage imageNamed:@"buttonBg"] forState:UIControlStateHighlighted];
    [self.confirmBtn addTarget:self action:@selector(didClickConfirmBtn:) forControlEvents:UIControlEventTouchUpInside];
    [self.confirmBtn setTitleColor:[UIColor redColor] forState:UIControlStateHighlighted];
    self.confirmBtn.hidden = YES;
    
    self.switchViewArray = [NSMutableArray array];
    
    self.lineView = [[UIView alloc] init];
    [self.contentView addSubview:self.lineView];
    self.lineView.backgroundColor = kThemeColorGray;
}

- (void)setModel:(ZegoCommonCellModel *)model {
    _model = model;
    self.titleLable.text = model.title;
    __weak typeof(self) weakSelf = self;
    if (model.options.count > 0) {
        self.titleLable.hidden = NO;
        self.confirmBtn.hidden = NO;
        for (ZegoCellOptionModel *option in model.options) {
            ZegoSettingSwitchView *view = [[ZegoSettingSwitchView alloc] init];
            [self addSubview:view];
            //两个状态根据设置获取
            if([option.title isEqualToString:@"接收缩放同步"]){
                option.value = @([ZegoWhiteboardManager sharedInstance].enableResponseScale);
            }
            if([option.title isEqualToString:@"发送缩放同步"]){
                option.value = @([ZegoWhiteboardManager sharedInstance].enableSyncScale);
            }
            if ([option.title isEqualToString:@"开启笔锋"]) {
                option.value = @([ZegoWhiteboardManager sharedInstance].enableHandwriting);
            }
        
            view.titleLabel.text = option.title;
            view.stateSwitch.on = [option.value boolValue];
          
            
            [self.switchViewArray addObject:view];
            view.didClickSwitchBlock = ^(BOOL state) {
                __strong typeof(weakSelf) strongSelf = weakSelf;
                [strongSelf changeSwitchStatus:option state:state];
            };
        }
    } else {
        self.titleLable.hidden = YES;
        self.confirmBtn.hidden = YES;
        ZegoSettingSwitchView *view = [[ZegoSettingSwitchView alloc] init];
        [self addSubview:view];
        view.titleLabel.text = model.title;
        view.stateSwitch.on = [model.value boolValue];
        [self.switchViewArray addObject:view];
        view.didClickSwitchBlock = ^(BOOL state) {
            __strong typeof(weakSelf) strongSelf = weakSelf;
            strongSelf.model.value = [NSNumber numberWithBool:state];
            if ([strongSelf.delegate respondsToSelector:@selector(onSettingCellValueChange:)]) {
                [strongSelf.delegate onSettingCellValueChange:strongSelf.model];
            }
        };
    }
}

- (void)changeSwitchStatus:(ZegoCellOptionModel *)option state:(BOOL)state {
    option.value = [NSNumber numberWithBool:state];
    if (state) {
        if ([option.title isEqualToString:@"滚动模式"]) {
            [self findOptionModel:option.title];
        } else if ([option.title isEqualToString:@"绘制模式"]) {
            [self findOptionModel:option.title];
        } else if ([option.title isEqualToString:@"放缩模式"]) {
            [self findOptionModel:option.title];
        } else if ([option.title isEqualToString:@"不可操作模式"]) {
            [self findOptionModel:option.title];
        }
    }
}

- (BOOL)findUnOperationValue {
    BOOL operationValue = NO;
    for (ZegoCellOptionModel *option in self.model.options) {
        if ([option.title isEqualToString:@"不可操作模式"]) {
            operationValue = [option.value boolValue];
            break;
        }
    }
    return operationValue;
}

- (void)findOptionModel:(NSString *)title {
    for (ZegoCellOptionModel *optionModel in self.model.options) {
        if ([title isEqualToString:@"滚动模式"] && ([optionModel.title isEqualToString:@"绘制模式"] || [optionModel.title isEqualToString:@"不可操作模式"]) && [optionModel.value boolValue]) {
            //滚动模式不可与绘制模式共存
            optionModel.value = [NSNumber numberWithBool:NO];
            [self findSwitchView:title];
        } else if ([title isEqualToString:@"绘制模式"] && ([optionModel.title isEqualToString:@"滚动模式"]||[optionModel.title isEqualToString:@"不可操作模式"]) && [optionModel.value boolValue]) {
            //绘制模式和滚动模式不可共存
            optionModel.value = [NSNumber numberWithBool:NO];
            [self findSwitchView:title];
        } else if ([title isEqualToString:@"不可操作模式"] && ![optionModel.title isEqualToString:@"不可操作模式"]) {
            //不可操作模式和其他模式不共存
            if ([optionModel.title isEqualToString:@"接收缩放同步"] || [optionModel.title isEqualToString:@"发送缩放同步"]) {
                return;
            }
            optionModel.value = [NSNumber numberWithBool:NO];
            [self findSwitchView:title];
        } else if ([title isEqualToString:@"放缩模式"] && [optionModel.title isEqualToString:@"不可操作模式"]) {
            optionModel.value = [NSNumber numberWithBool:NO];
            [self findSwitchView:title];
        }
    }
}

- (void)findSwitchView:(NSString *)title {
    for (ZegoSettingSwitchView *view in self.switchViewArray) {
        if ([title isEqualToString:@"滚动模式"] && ([view.titleLabel.text isEqualToString:@"绘制模式"] || [view.titleLabel.text isEqualToString:@"不可操作模式"])) {
            if ([view.titleLabel.text isEqualToString:@"不可操作模式"]) {
                [ZegoProgessHUD showTipMessage:@"绘制和滚动模式无法同时开启，仅生效滚动模式"];
            }
            view.stateSwitch.on = NO;
        } else if ([title isEqualToString:@"绘制模式"] && ([view.titleLabel.text isEqualToString:@"滚动模式"] || [view.titleLabel.text isEqualToString:@"不可操作模式"])) {
            if ([view.titleLabel.text isEqualToString:@"不可操作模式"]) {
                [ZegoProgessHUD showTipMessage:@"绘制和滚动模式无法同时开启，仅生效滚动模式"];
            }
            view.stateSwitch.on = NO;
        } else if ([title isEqualToString:@"不可操作模式"] && ![view.titleLabel.text isEqualToString:@"不可操作模式"]) {
            //不可操作模式和其他模式不共存
            [ZegoProgessHUD showTipMessage:@"不可操作模式下，不支持其他操作模式"];
            if ([view.titleLabel.text isEqualToString:@"接收缩放同步"] || [view.titleLabel.text isEqualToString:@"发送缩放同步"]) {
                return;
            }
            view.stateSwitch.on = NO;
        } else if ([title isEqualToString:@"放缩模式"] && [view.titleLabel.text isEqualToString:@"不可操作模式"]) {
            view.stateSwitch.on = NO;
        }
    }
}

- (void)didClickConfirmBtn:(UIButton *)sender {
    if ([self.delegate respondsToSelector:@selector(onSettingCellValueChange:)]) {
        if (self.model.options.count > 0) {
            for (int i = 0; i < self.model.options.count; i++) {
                ZegoCellOptionModel *model = self.model.options[i];
                ZegoSettingSwitchView *view = self.switchViewArray[i];
                model.value = [NSNumber numberWithBool:view.stateSwitch.isOn];
            }
        } else {
            ZegoSettingSwitchView *view = self.switchViewArray.firstObject;
            self.model.value = [NSNumber numberWithBool:view.stateSwitch.isOn];
        }
        [self.delegate onSettingCellValueChange:self.model];
    }
}

- (void)layoutSubviews {
    [super layoutSubviews];
    CGFloat margin = 10;
    
    if (self.model.options.count > 0) {
        [self.titleLable mas_makeConstraints:^(MASConstraintMaker *make) {
            make.leading.top.equalTo(self.contentView).offset(margin);
            make.trailing.equalTo(self.contentView).offset(-50);
            make.height.mas_equalTo(20);
        }];
        [self.confirmBtn mas_makeConstraints:^(MASConstraintMaker *make) {
            make.trailing.equalTo(self.contentView).offset(-margin);
            make.width.height.mas_equalTo(30);
            make.centerY.equalTo(self.titleLable);
        }];
        
        [self.lineView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.bottom.left.right.equalTo(self.contentView);
            make.height.mas_equalTo(0.5);
        }];
        
        ZegoSettingSwitchView *previousView = nil;
        for (ZegoSettingSwitchView *view in self.switchViewArray) {
            
            [view mas_makeConstraints:^(MASConstraintMaker *make) {
                if (previousView) {
                    make.top.equalTo(previousView.mas_bottom).offset(margin);
                } else {
                    make.top.equalTo(self.titleLable.mas_bottom).offset(margin);
                }
                make.trailing.equalTo(self.contentView).offset(-margin);
                make.leading.equalTo(self.titleLable);
                make.height.mas_equalTo(30);
                
            }];
            previousView = view;
        }
    } else {
        ZegoSettingSwitchView *view = self.switchViewArray.firstObject;
        [view mas_makeConstraints:^(MASConstraintMaker *make) {
            make.top.left.equalTo(self).offset(margin);
            make.right.bottom.equalTo(self).offset(-margin);
        }];
    }
    
}
+ (CGFloat)getSwitchCellHeightWithModel:(ZegoCommonCellModel *)model {

    CGFloat totalHeight = model.options.count * 40 + 40;
    return totalHeight;
}

@end

@implementation ZegoSettingSwitchView

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        [self setupUI];
    }
    return self;
}

- (void)setupUI {
    self.titleLabel = [[UILabel alloc] init];
    [self addSubview: self.titleLabel];
    self.titleLabel.textColor = kTextColor1;
    self.titleLabel.font = kFontText12;
    
    self.stateSwitch = [[UISwitch alloc] init];
    [self addSubview:self.stateSwitch];
    self.stateSwitch.on = NO;
    [self.stateSwitch addTarget:self action:@selector(didSwitch:) forControlEvents:UIControlEventValueChanged];
}

- (void)didSwitch:(UISwitch *)sender {
    if (self.didClickSwitchBlock) {
        self.didClickSwitchBlock(sender.isOn);
    }
}

- (void)layoutSubviews
{
    [super layoutSubviews];
    CGFloat margin = 0;
    [self.stateSwitch mas_makeConstraints:^(MASConstraintMaker *make) {
        make.trailing.equalTo(self).offset(-margin);
        make.centerY.equalTo(self);
    }];
    [self.titleLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.leading.equalTo(self).offset(margin);
        make.centerY.equalTo(self);
        make.trailing.equalTo(self.stateSwitch.mas_leading).offset(margin);
    }];
}

- (void)dealloc {
//    DLog(@" %@ dealloc",self.class);
}
@end
