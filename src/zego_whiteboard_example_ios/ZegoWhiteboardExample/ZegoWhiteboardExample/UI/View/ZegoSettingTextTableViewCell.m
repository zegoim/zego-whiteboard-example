//
//  ZegoSettingTextTableViewCell.m
//  ZegoWhiteboardExample
//
//  Created by Xuyang Nie on 2020/11/26.
//

#import "ZegoSettingTextTableViewCell.h"


@interface ZegoSettingTextTableViewCell()<UITextFieldDelegate>
@property (nonatomic, strong) UILabel *titleLable;
@property (nonatomic, strong) NSMutableArray *inputArray;
@property (nonatomic, strong) UIButton *confirmBtn;
@property (nonatomic, strong) UIView *lineView;
@end
@implementation ZegoSettingTextTableViewCell

- (instancetype)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    if (self = [super initWithStyle:style reuseIdentifier:reuseIdentifier]) {
        self.inputArray = [NSMutableArray array];
        [self setupUI];
    }
    return self;
}

- (void)prepareForReuse {
    [super prepareForReuse];
    self.titleLable.text = nil;
    for (UITextField *tf in self.inputArray) {
        [tf removeFromSuperview];
    }
    [self.inputArray removeAllObjects];
}

- (void)setupUI {
    self.selectionStyle = UITableViewCellSelectionStyleNone;
    self.titleLable = [[UILabel alloc] init];
    [self.contentView addSubview: self.titleLable];
    self.titleLable.textColor = kTextColor1;
    self.titleLable.font = kFontText14;
    
    self.confirmBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    [self.contentView addSubview:self.confirmBtn];
    self.confirmBtn.titleLabel.font = [UIFont systemFontOfSize:14];
    [self.confirmBtn setTitle:@"确定" forState:UIControlStateNormal];
    [self.confirmBtn setTitleColor:kThemeColorPink forState:UIControlStateNormal];
    [self.confirmBtn setBackgroundImage:[UIImage imageNamed:@"buttonBg"] forState:UIControlStateHighlighted];
    [self.confirmBtn addTarget:self action:@selector(didClickConfirmBtn:) forControlEvents:UIControlEventTouchUpInside];
    [self.confirmBtn setTitleColor:[UIColor redColor] forState:UIControlStateHighlighted];
    
    self.lineView = [[UIView alloc] init];
    [self.contentView addSubview:self.lineView];
    self.lineView.backgroundColor = kThemeColorGray;
}

- (void)setModel:(ZegoCommonCellModel *)model {
    _model = model;
    self.titleLable.text = model.title;
    if (model.options.count > 0) {
        for (ZegoCellOptionModel *option in model.options) {
            ZegoSettingTextView *view = [[ZegoSettingTextView alloc] init];
            [self addSubview:view];
            view.titleLabel.text = option.title;
            view.inputTF.placeholder = option.defaultValue;
            view.inputTF.text = option.value;
            [self.inputArray addObject:view];
        }
    } else {
        ZegoSettingTextView *view = [[ZegoSettingTextView alloc] init];
        [self addSubview:view];
        view.inputTF.placeholder = model.placeholder;
        view.inputTF.text = model.value;
        [self.inputArray addObject:view];
    }
}

- (void)didClickConfirmBtn:(UIButton *)sender {
    if ([self.delegate respondsToSelector:@selector(onSettingCellValueChange:)]) {
        if (self.model.options.count > 0) {
            for (int i = 0; i < self.model.options.count; i++) {
                ZegoCellOptionModel *model = self.model.options[i];
                ZegoSettingTextView *view = self.inputArray[i];
                model.value = view.inputTF.text;
            }
        } else {
            ZegoSettingTextView *view = self.inputArray.firstObject;
            self.model.value = view.inputTF.text;
            self.model.placeholder = self.model.value;
        }
        [self.delegate onSettingCellValueChange:self.model];
    }
}

- (void)layoutSubviews {
    [super layoutSubviews];
    CGFloat margin = 10;
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
    if (self.model.options.count > 0) {
        ZegoSettingTextView *previousView = nil;
        for (ZegoSettingTextView *view in self.inputArray) {
            
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
        ZegoSettingTextView *view = self.inputArray.firstObject;
        [view mas_makeConstraints:^(MASConstraintMaker *make) {
            make.top.equalTo(self.titleLable.mas_bottom).offset(margin);
            
            make.trailing.equalTo(self.contentView).offset(-margin);
            make.leading.equalTo(self.titleLable);
            make.height.mas_equalTo(30);
            
        }];
    }
    
}
+ (CGFloat)getTextCellHeightWithModel:(ZegoCommonCellModel *)model {

    CGFloat totalHeight = (model.options.count?:1) * (35) + 50;
    return totalHeight;
}
@end

@implementation ZegoSettingTextView

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        [self setupUI];
    }
    return self;
}

- (void)setupUI {
    self.titleLabel = [[UILabel alloc] init];
    [self addSubview:self.titleLabel];
    self.titleLabel.font = [UIFont systemFontOfSize:12];
    self.titleLabel.textColor = [UIColor blackColor];
    
    self.inputTF = [[UITextField alloc] init];
    [self addSubview:self.inputTF];
    self.inputTF.font = [UIFont systemFontOfSize:12];
    self.inputTF.borderStyle = UITextBorderStyleRoundedRect;
    self.inputTF.clearButtonMode = UITextFieldViewModeWhileEditing;
}

- (void)layoutSubviews {
    [super layoutSubviews];
    [self.titleLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.top.bottom.equalTo(self);
        if (self.titleLabel.text.length > 0) {
            make.width.mas_equalTo(60);
        } else {
            make.width.mas_equalTo(0);
        }
    }];
    
    [self.inputTF mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.equalTo(self.titleLabel.mas_right).offset(5);
        make.top.bottom.equalTo(self.titleLabel);
        make.right.equalTo(self);
    }];
}

- (void)dealloc {
//    DLog(@" %@ dealloc",self.class);
}
@end
