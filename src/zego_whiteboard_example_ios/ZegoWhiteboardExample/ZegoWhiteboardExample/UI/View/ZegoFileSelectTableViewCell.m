//
//  ZegoFileSelectTableViewCell.m
//  ZegoWhiteboardExample
//
//  Created by MartinNie on 2020/12/2.
//

#import "ZegoFileSelectTableViewCell.h"
@interface ZegoFileSelectTableViewCell ()
@property (nonatomic, strong) UILabel *titleLabel;
@property (nonatomic, strong) UIButton *deleteBtn;
@end
@implementation ZegoFileSelectTableViewCell

- (instancetype)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    if (self = [super initWithStyle:style reuseIdentifier:reuseIdentifier]) {
        [self setupUI];
    }
    return self;
}

- (void)setupUI {
    self.titleLabel = [[UILabel alloc] init];
    [self.contentView addSubview:self.titleLabel];
    self.titleLabel.font = [UIFont systemFontOfSize:12];
    self.titleLabel.textColor = [UIColor blackColor];
    
    self.deleteBtn = [UIButton buttonWithType:UIButtonTypeSystem];
    [self.contentView addSubview:self.deleteBtn];
    [self.deleteBtn addTarget:self action:@selector(didClickDeleteBtn:) forControlEvents:UIControlEventTouchUpInside];
    [self.deleteBtn setTitle:@"X" forState:UIControlStateNormal];
    self.deleteBtn.layer.cornerRadius = 10;
    self.deleteBtn.clipsToBounds = YES;
    
}

- (void)setTitle:(NSString *)title {
    _title = title;
    self.titleLabel.text = title;
}

- (void)setSelectedStyle:(BOOL)selectedStyle {
    _selectedStyle = selectedStyle;
    if (selectedStyle) {
        self.contentView.backgroundColor = kThemeColorBlue;
    } else {
        self.contentView.backgroundColor = [UIColor whiteColor];
    }
}

- (void)didClickDeleteBtn:(UIButton *)sender {
    if (self.didClickDeleteBlock) {
        self.didClickDeleteBlock();
    }
}

- (void)layoutSubviews {
    [super layoutSubviews];
    [self.titleLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.equalTo(self.contentView).offset(12);
        make.top.bottom.equalTo(self.contentView).offset(5);
        make.right.equalTo(self.deleteBtn.mas_left);
    }];
    [self.deleteBtn mas_makeConstraints:^(MASConstraintMaker *make) {
        make.centerY.equalTo(self.contentView);
        make.right.equalTo(self.contentView).offset(-12);
        make.width.height.mas_equalTo(20);
    }];
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
