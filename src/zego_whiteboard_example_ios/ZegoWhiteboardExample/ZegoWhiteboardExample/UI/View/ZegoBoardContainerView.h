//
//  ZegoBoardContainerView.h
//  ZegoWhiteboardExample
//
//  Created by Xuyang Nie on 2020/11/27.
//

#import <UIKit/UIKit.h>
#import <ZegoWhiteboardView/ZegoWhiteboardView.h>
NS_ASSUME_NONNULL_BEGIN
@protocol ZegoBoardContainerViewDelegate <NSObject>

- (void)onLoadFileFinish:(ZegoWhiteboardView *)whiteboardView docsView:(ZegoDocsView *)docsView currentPage:(NSInteger)currentPage;
- (void)onScrollWithCurrentPage:(NSInteger )currentPage totalPage:(NSInteger)totalPage;
@end

@interface ZegoBoardContainerView : UIView
@property (nonatomic, weak) id <ZegoBoardContainerViewDelegate> delegate;

@property (nonatomic, strong,readonly) NSMutableArray *docsViewArray;

@property (nonatomic, strong)NSDictionary *authInfo;

//将白板添加到视图顶部并显示
- (void)addWhiteboardView:(ZegoWhiteboardView *)whiteboardView;
//移除白板
- (void)removeWhiteboardWithID:(ZegoWhiteboardID)whiteboardID;

@end

NS_ASSUME_NONNULL_END
