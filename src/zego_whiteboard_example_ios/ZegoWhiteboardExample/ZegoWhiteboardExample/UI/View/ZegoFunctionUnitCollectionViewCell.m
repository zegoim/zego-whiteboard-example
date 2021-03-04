//
//  ZegoFunctionUnitCollectionViewCell.m
//  ZegoWhiteboardExample
//
//  Created by Xuyang Nie on 2020/11/28.
//

#import "ZegoFunctionUnitCollectionViewCell.h"
@interface ZegoFunctionUnitCollectionViewCell()
@property (nonatomic, strong) UILabel *titleLabel;

@end
@implementation ZegoFunctionUnitCollectionViewCell
- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        [self setupUI];
    }
    return self;
}

- (void)setupUI {
    self.titleLabel = [[UILabel alloc] init];
    [self.contentView addSubview:self.titleLabel];
    self.titleLabel.font = [UIFont systemFontOfSize:14];
    self.titleLabel.textColor = [UIColor whiteColor];
    self.titleLabel.backgroundColor = kThemeColorBlue;
    self.titleLabel.textAlignment = NSTextAlignmentCenter;
}

- (void)setTitle:(NSString *)title {
    _title = title;
    self.titleLabel.text = title;
}

- (void)layoutSubviews {
    [super layoutSubviews];
    
    [self.titleLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.left.bottom.right.equalTo(self.contentView);
    }];
    
}

- (void)dealloc {
//    DLog(@" %@ dealloc",self.class);
}
@end
