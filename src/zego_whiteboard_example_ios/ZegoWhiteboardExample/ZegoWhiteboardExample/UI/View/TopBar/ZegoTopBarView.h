//
//  ZegoTopBarView.h
//  ZegoWhiteboardExample
//
//  Created by Xuyang Nie on 2020/11/27.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN
@protocol ZegoTopBarViewDelegate <NSObject>

- (void)onShowWhiteboardSelect;

@end

@interface ZegoTopBarView : UIView
@property (nonatomic, weak) id <ZegoTopBarViewDelegate> delegate;
- (void)setupBboardName:(NSString *)boardName;
- (void)setupCurrentPage:(NSInteger)currentPage totalCount:(NSInteger)totalCount;
@end

NS_ASSUME_NONNULL_END
