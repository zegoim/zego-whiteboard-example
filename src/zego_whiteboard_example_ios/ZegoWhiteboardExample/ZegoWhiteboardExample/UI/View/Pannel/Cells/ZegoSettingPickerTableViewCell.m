//
//  ZegoSettingPickerTableViewCell.m
//  ZegoWhiteboardExample
//
//  Created by Xuyang Nie on 2020/11/26.
//

#import "ZegoSettingPickerTableViewCell.h"
#import "ZegoSelecteSheetManager.h"
@interface ZegoSettingPickerTableViewCell()<UIPickerViewDelegate,UIPickerViewDataSource>
@property (nonatomic, strong) UILabel *titleLable;
@property (nonatomic, strong) UIButton *pickerBtn;
@property (nonatomic, strong) UIImageView *selectIV;
@property (nonatomic, strong) UIView *lineView;
@end
@implementation ZegoSettingPickerTableViewCell
- (instancetype)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    if (self = [super initWithStyle:style reuseIdentifier:reuseIdentifier]) {
        [self setupUI];
    }
    return self;
}

- (void)prepareForReuse {
    [super prepareForReuse];
    self.titleLable.text = nil;
    [self.pickerBtn setTitle:@"" forState:UIControlStateNormal];
}

- (void)setupUI {
    self.selectionStyle = UITableViewCellSelectionStyleNone;
    self.titleLable = [[UILabel alloc] init];
    [self.contentView addSubview: self.titleLable];
    self.titleLable.textColor = kTextColor1;
    self.titleLable.font = kFontText14;
    
    self.selectIV = [[UIImageView alloc] init];
    [self.contentView addSubview:self.selectIV];
    self.selectIV.image = [UIImage imageNamed:@"arrow_down"];
    
    self.pickerBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    [self.contentView addSubview:self.pickerBtn];
    [self.pickerBtn addTarget:self action:@selector(didClickPickerBtn:) forControlEvents:UIControlEventTouchUpInside];
    [self.pickerBtn setTitleColor:kThemeColorPink forState:UIControlStateNormal];
    self.pickerBtn.titleLabel.font = [UIFont systemFontOfSize:12];
    self.pickerBtn.layer.borderWidth = 1;
    self.pickerBtn.layer.borderColor = kThemeColorGray.CGColor;
    
    self.lineView = [[UIView alloc] init];
    [self.contentView addSubview:self.lineView];
    self.lineView.backgroundColor = kThemeColorGray;

}
- (void)setModel:(ZegoCommonCellModel *)model {
    _model = model;
    _titleLable.text = model.title;
    if ([model.value integerValue] >= 0 && model.options.count > 0) {
        ZegoCellOptionModel *option = self.model.options[[model.value integerValue]];
        [self.pickerBtn setTitle:option.title forState:UIControlStateNormal];
    }

}

- (void)didClickPickerBtn:(UIButton *)sender {
    __weak typeof(self) weakSelf = self;
    [[ZegoSelecteSheetManager shareManager] showSheetWithTitle:self.model.title optionArray:self.model.options selectedBlock:^(NSInteger index) {
        __strong typeof(weakSelf) strongSelf = weakSelf;
        ZegoCellOptionModel *option = strongSelf.model.options[index];
        strongSelf.model.value = [NSNumber numberWithInteger:index];
        [strongSelf.pickerBtn setTitle:option.title forState:UIControlStateNormal];
        if ([strongSelf.delegate respondsToSelector:@selector(onSettingCellValueChange:)]) {
            [strongSelf.delegate onSettingCellValueChange:strongSelf.model];
        }
    }];
}



- (void)layoutSubviews {
    [super layoutSubviews];
    CGFloat margin = 10;
    [self.selectIV mas_makeConstraints:^(MASConstraintMaker *make) {
        make.right.equalTo(self.pickerBtn).offset(-margin);
        make.centerY.equalTo(self.pickerBtn);
        make.width.height.mas_equalTo(12);
    }];
    
    [self.pickerBtn mas_makeConstraints:^(MASConstraintMaker *make) {
        make.trailing.equalTo(self.contentView).offset(-margin);
        make.top.equalTo(self.contentView).offset(margin);
        make.bottom.equalTo(self.contentView).offset(-margin);
        make.leading.equalTo(self.titleLable.mas_trailing).offset(margin);
    }];
    [self.titleLable mas_makeConstraints:^(MASConstraintMaker *make) {
        make.leading.equalTo(self.contentView).offset(margin);
        make.centerY.equalTo(self.contentView);
        make.width.mas_equalTo(100);
    }];
    
    [self.lineView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.bottom.left.right.equalTo(self.contentView);
        make.height.mas_equalTo(0.5);
    }];

}

+ (CGFloat)getPickerCellHeight {
    return 44;
}

- (void)awakeFromNib {
    [super awakeFromNib];
    // Initialization code
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

- (void)dealloc {
//    DLog(@" %@ dealloc",self.class);
}
@end
