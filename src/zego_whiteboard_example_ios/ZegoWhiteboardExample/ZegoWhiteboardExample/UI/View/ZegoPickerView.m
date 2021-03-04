//
//  ZegoPickerView.m
//  ZegoWhiteboardExample
//
//  Created by MartinNie on 2020/12/1.
//

#import "ZegoPickerView.h"
@interface ZegoPickerView ()<UIPickerViewDelegate>
@property (nonatomic, strong) UIPickerView *pickerView;
@property (nonatomic, assign) NSInteger selectedIndex;
@property (nonatomic, strong) NSArray *optionArray;
@end
@implementation ZegoPickerView

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        
    }
    return self;
}

- (void)setupUI {
    self.pickerView = [[UIPickerView alloc] init];
    [self addSubview:self.pickerView];
    self.pickerView.delegate = self;
}

- (void)showPickerViewWithData:(NSArray *)optionArray {
    self.optionArray = optionArray;
    [self.pickerView reloadAllComponents];
    [[UIApplication sharedApplication].keyWindow addSubview:self];
    self.frame = CGRectMake(0, 0, kScreenWidth, kScreenHeight);
    self.alpha = 0;
    [UIView animateWithDuration:0.5 animations:^{
        self.alpha = 1;
    }];
}

- (void)hiddenPickerView {
    [UIView animateWithDuration:0.5 animations:^{
        self.alpha = 0;
    } completion:^(BOOL finished) {
        [self removeFromSuperview];
    }];
}

- (NSInteger)numberOfComponentsInPickerView:(UIPickerView *)pickerView
{
    return 0;
}

- (NSInteger)pickerView:(UIPickerView *)pickerView numberOfRowsInComponent:(NSInteger)component
{
    return self.optionArray.count;
}

- (NSString *)pickerView:(UIPickerView *)pickerView titleForRow:(NSInteger)row forComponent:(NSInteger)component
{
    NSString *title = self.optionArray[row];
    return title;
}

- (void)pickerView:(UIPickerView *)pickerView didSelectRow:(NSInteger)row inComponent:(NSInteger)component {
    self.selectedIndex = row;
}

- (void)dealloc {
    DLog(@" %@ dealloc",self.class);
}
@end
