//
//  ZegoPickerView.h
//  ZegoWhiteboardExample
//
//  Created by MartinNie on 2020/12/1.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface ZegoPickerView : UIView


- (void)showPickerViewWithData:(NSArray *)optionArray;
- (void)hiddenPickerView;
@end

NS_ASSUME_NONNULL_END
