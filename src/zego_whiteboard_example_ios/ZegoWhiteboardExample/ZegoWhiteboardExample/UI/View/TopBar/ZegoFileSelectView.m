//
//  ZegoFileSelectView.m
//  ZegoWhiteboardExample
//
//  Created by MartinNie on 2020/12/1.
//

#import "ZegoFileSelectView.h"
#import "ZegoFileSelectTableViewCell.h"
@interface ZegoFileSelectView ()<UITableViewDataSource,UITableViewDelegate>
@property (nonatomic, strong) UITableView *tableView;

@end
@implementation ZegoFileSelectView

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
       
        [self setupUI];
    }
    return self;
}

- (void)setupUI {
    self.backgroundColor = [UIColor colorWithWhite:0 alpha:0.3];
    self.tableView = [[UITableView alloc] initWithFrame:CGRectMake(self.bounds.size.width,0, kFunctionPannelViewWidth, self.bounds.size.height) style:UITableViewStylePlain];
    [self addSubview:self.tableView];
    self.tableView.delegate = self;
    self.tableView.dataSource = self;
    [self.tableView registerClass:[ZegoFileSelectTableViewCell class] forCellReuseIdentifier:@"cell"];
    self.selectedIndex = -1;
}

- (void)setFileList:(NSArray *)fileList {
    _fileList = fileList;
    [self.tableView reloadData];
}

- (void)touchesBegan:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event {
    [self hiddenFileSelectView];
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.fileList.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    
    ZegoFileSelectTableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"cell" forIndexPath:indexPath];
    ZegoWhiteboardView *view = self.fileList[indexPath.row];
    if (view.whiteboardModel.fileInfo.fileName.length > 0) {
        cell.title = view.whiteboardModel.fileInfo.fileName;
    } else {
        cell.title = view.whiteboardModel.name;
    }
    __weak typeof(self) weakSelf = self;
    cell.didClickDeleteBlock = ^{
        __strong typeof(weakSelf) strongSelf = weakSelf;
        NSMutableArray *temp = [NSMutableArray arrayWithArray:strongSelf.fileList];
        [temp removeObjectAtIndex:indexPath.row];
        strongSelf.fileList = temp.copy;
        [strongSelf.tableView reloadData];
        if (indexPath.row == self.selectedIndex) {
            strongSelf.selectedIndex = -1;
        }
        if ([strongSelf.delegate respondsToSelector:@selector(onRemoveWhiteboardIdnex:)]) {
            [strongSelf.delegate onRemoveWhiteboardIdnex:indexPath.row];
        }
        //清除远端白板
        [[ZegoBoardOperationManager shareManager] removeBoardWithID:view.whiteboardModel.whiteboardID];
    };
    if (indexPath.row == self.selectedIndex) {
        cell.selectedStyle = YES;
    } else {
        cell.selectedStyle = NO;
    }
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    self.selectedIndex = indexPath.row;
    [self hiddenFileSelectView];
    ZegoFileSelectTableViewCell *cell = [tableView cellForRowAtIndexPath:indexPath];
    cell.selectedStyle = YES;
    if ([self.delegate respondsToSelector:@selector(onSelectedWhiteboardIndex:)]) {
        [self.delegate onSelectedWhiteboardIndex:indexPath.row];
    }
    
}

- (void)tableView:(UITableView *)tableView didDeselectRowAtIndexPath:(NSIndexPath *)indexPath {
    ZegoFileSelectTableViewCell *cell = [tableView cellForRowAtIndexPath:indexPath];
    cell.selectedStyle = NO;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return 44;
}


- (void)showFileSelectView {
    if (_isShow) {
        return;
    }
    _isShow = YES;
    [[UIApplication sharedApplication].keyWindow addSubview:self];
    self.tableView.frame = CGRectMake(kScreenWidth,0, kFunctionPannelViewWidth, kScreenHeight);
    [UIView animateWithDuration:0.5 animations:^{
        if (@available(iOS 11.0, *)) {
            self.tableView.frame = CGRectMake(kScreenWidth - kFunctionPannelViewWidth - self.safeAreaInsets.right,0, kFunctionPannelViewWidth, kScreenHeight);
        } else {
            self.tableView.frame = CGRectMake(kScreenWidth - kFunctionPannelViewWidth ,0, kFunctionPannelViewWidth, kScreenHeight);
        }
    }];
}

- (void)hiddenFileSelectView {
    _isShow = NO;
    [UIView animateWithDuration:0.5 animations:^{
        self.tableView.frame = CGRectMake(kScreenWidth,0, kFunctionPannelViewWidth, kScreenHeight);
    } completion:^(BOOL finished) {
        [self removeFromSuperview];
    }];
}

- (void)dealloc {
    DLog(@" %@ dealloc",self.class);
}
@end
