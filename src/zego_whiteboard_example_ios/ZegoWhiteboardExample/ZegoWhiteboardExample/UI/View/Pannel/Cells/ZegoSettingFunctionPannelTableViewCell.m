//
//  ZegoSettingFunctionPannelTableViewCell.m
//  ZegoWhiteboardExample
//
//  Created by Xuyang Nie on 2020/11/26.
//

#import "ZegoSettingFunctionPannelTableViewCell.h"
#import "ZegoFunctionUnitCollectionViewCell.h"
@interface ZegoSettingFunctionPannelTableViewCell()<UICollectionViewDelegate,UICollectionViewDataSource>
@property (nonatomic, strong) UILabel *titleLable;
@property (nonatomic, strong) UICollectionView *collectionView;
@property (nonatomic, weak) NSArray *functionArray;
@property (nonatomic, strong) UIView *lineView;

@end
@implementation ZegoSettingFunctionPannelTableViewCell

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
    self.functionArray = nil;
    [self.collectionView reloadData];
}

- (void)setupUI {
    self.selectionStyle = UITableViewCellSelectionStyleNone;
    self.titleLable = [[UILabel alloc] init];
    [self.contentView addSubview: self.titleLable];
    self.titleLable.textColor = kTextColor1;
    self.titleLable.font = kFontText14;
    
    UICollectionViewFlowLayout *flowLayout = [UICollectionViewFlowLayout new];
    flowLayout.itemSize = CGSizeMake(70, 25);
    flowLayout.minimumLineSpacing = 10;
    flowLayout.minimumInteritemSpacing = 10;
    flowLayout.scrollDirection = UICollectionViewScrollDirectionVertical;
    self.collectionView = [[UICollectionView alloc] initWithFrame:self.bounds collectionViewLayout:flowLayout];
    [self.contentView addSubview:self.collectionView];
    [self.collectionView registerClass:[ZegoFunctionUnitCollectionViewCell class] forCellWithReuseIdentifier:@"cell"];
    self.collectionView.delegate = self;
    self.collectionView.dataSource = self;
    self.collectionView.backgroundColor = [UIColor whiteColor];
    self.collectionView.showsVerticalScrollIndicator = YES;
    
    self.lineView = [[UIView alloc] init];
    [self.contentView addSubview:self.lineView];
    self.lineView.backgroundColor = kThemeColorGray;
}

- (void)setModel:(ZegoCommonCellModel *)model {
    _model = model;
    _titleLable.text = model.title;
    self.functionArray = model.options;
    [self.collectionView reloadData];
}

- (NSInteger)numberOfSectionsInCollectionView:(UICollectionView *)collectionView {
    return 1;
}

- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section
{
    return self.functionArray.count;
}

- (__kindof UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath {
    ZegoCellOptionModel *option = self.functionArray[indexPath.row];
    ZegoFunctionUnitCollectionViewCell *cell = [collectionView dequeueReusableCellWithReuseIdentifier:@"cell" forIndexPath:indexPath];
    cell.title = option.title;
    return cell;
}

- (void)collectionView:(UICollectionView *)collectionView didSelectItemAtIndexPath:(NSIndexPath *)indexPath {
    self.model.value = [NSNumber numberWithInteger:indexPath.row];
    if ([self.delegate respondsToSelector:@selector(onSettingCellValueChange:)]) {
        [self.delegate onSettingCellValueChange:self.model];
    }
}


- (void)layoutSubviews {
    [super layoutSubviews];
    CGFloat margin = 10;
    [self.titleLable mas_makeConstraints:^(MASConstraintMaker *make) {
        make.leading.top.equalTo(self.contentView).offset(margin);
        make.trailing.equalTo(self.contentView).offset(-margin);
        make.height.mas_equalTo(17);
    }];
    [self.collectionView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.leading.trailing.equalTo(self.titleLable);
        make.top.equalTo(self.titleLable.mas_bottom).offset(margin);
        make.bottom.equalTo(self.contentView).offset(-margin);
    }];
    
    [self.lineView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.bottom.left.right.equalTo(self.contentView);
        make.height.mas_equalTo(0.5);
    }];
}

+ (CGFloat)getFunctionPannelCellHeight {
    return 110;
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
