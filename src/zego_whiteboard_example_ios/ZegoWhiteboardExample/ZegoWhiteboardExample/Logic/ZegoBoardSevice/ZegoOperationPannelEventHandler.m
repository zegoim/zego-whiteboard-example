//
//  ZegoOperationPannelEventHandler.m
//  ZegoWhiteboardExample
//
//  Created by MartinNie on 2020/12/1.
//

#import "ZegoOperationPannelEventHandler.h"
#import "NSString+FormatValidator.h"

@interface ZegoOperationPannelEventHandler ()<UIDocumentPickerDelegate,UINavigationControllerDelegate,UIImagePickerControllerDelegate>
@property (nonatomic, assign) ZegoDocsViewRenderType currentRenderType;
@property (nonatomic, assign) ZegoSeq currentUploadSeq;
@property (nonatomic, assign) ZegoSeq currentCacheSeq;
@property (nonatomic, copy) NSString *currentFileID;
@property (nonatomic, assign) CGPoint imagePoint;
@property (nonatomic, strong) void(^documentSelectorBlock)(NSURL *fileUrl);
@end

@implementation ZegoOperationPannelEventHandler
{
    NSNumber *_backgroundMode;
}
//所有操作视图cell 时间处理中心，根据model 的type 分类处理
//根据model 中的eventNumber 处理指定事件
- (void)onSettingCellValueChange:(ZegoCommonCellModel *)valueChangeModel {
    DLog(@"EventHandler>>> onSettingCellValueChange:%lu",(unsigned long)valueChangeModel.type);
    switch (valueChangeModel.type) {
        case ZegoSettingTableViewCellTypeSwitch:
            [self handleSwitchEventWithModel:valueChangeModel];
            break;
            
        case ZegoSettingTableViewCellTypePicker:
            [self handlePickerEventWithModel:valueChangeModel];
            break;
            
        case ZegoSettingTableViewCellTypeText:
            [self handleTextEventWithModel:valueChangeModel];
            break;
            
        case ZegoSettingTableViewCellTypeFunctionPannel:
            [self handleFunctionPannelEventWithModel:valueChangeModel];
            break;
            
        default:
            break;
    }
}

- (void)handleSwitchEventWithModel:(ZegoCommonCellModel *)model {
    DLog(@"EventHandler>>> handleSwitchEventWithModel:%lu",(unsigned long)model.eventNumber);
    switch (model.eventNumber) {
        case ZegoOperationEventFlagTypeOperationMode:
            [self handleEventOperationMode:model];
            break;
            
        case ZegoOperationEventFlagTypeFontType:
            [self handleEventFontType:model];
            break;
            
        case ZegoOperationEventFlagTypeStepAutoPaging:
            [[ZegoBoardOperationManager shareManager] setupSetpAutoPaging:[model.value boolValue]];
            break;
            
            
        default:
            break;
    }
    
}

- (void)handlePickerEventWithModel:(ZegoCommonCellModel *)model {
    ZegoCellOptionModel *optionModel = model.options[[model.value integerValue] ];
    DLog(@"EventHandler>>> handlePickerEventWithModel:%lu",(unsigned long)model.eventNumber);
    switch (model.eventNumber) {
        case ZegoOperationEventFlagTypeToolType:
            [[ZegoBoardOperationManager shareManager] setupToolType:[optionModel.value integerValue]];
            break;
            
        case ZegoOperationEventFlagTypeFontSize:
            [[ZegoBoardOperationManager shareManager] setupFontSize:[optionModel.value integerValue]];
            break;
            
        case ZegoOperationEventFlagTypeLineWidth:
            [[ZegoBoardOperationManager shareManager] setupDrawLineWidth:[optionModel.value integerValue]];
            break;
            
        case ZegoOperationEventFlagTypeSelectedGraphic:
            [self handleSelectGraphicWithModel:model];
            break;
        
        case ZegoOperationEventFlagTypeBackgroundMode:
            [self handleBackgroundModeWithModel:model];
            break;
            
        case ZegoOperationEventFlagTypePresetBackground:
            [self handlePresetBackgroundWithModel:model];
            break;
            
            
        default:
            break;
    }
}

- (void)handleTextEventWithModel:(ZegoCommonCellModel *)model {
    DLog(@"EventHandler>>> handleTextEventWithModel:%lu",(unsigned long)model.eventNumber);
    [[UIApplication sharedApplication].keyWindow endEditing:YES];
    switch (model.eventNumber) {
        case ZegoOperationEventFlagTypeDrawColor:
            [[ZegoBoardOperationManager shareManager] setupColor:model.value];
            break;
  
        case ZegoOperationEventFlagTypeSpecialTextGraphic:
            [self handleEventSpecialTextGraphic:model];
            break;
            
        case ZegoOperationEventFlagTypeDefaultText:
            [[ZegoBoardOperationManager shareManager] setupCustomText:model.value];
            break;
            
        case ZegoOperationEventFlagTypeWhiteboardAdd:
            [[ZegoBoardOperationManager shareManager] addNewWhiteboardWithName:model.value fileID:@""];
            break;
        
        case ZegoOperationEventFlagTypeWhiteboardClearCache:
            [[ZegoBoardOperationManager shareManager] clearWhiteboardCache];
            break;
            
        case ZegoOperationEventFlagTypeUploadGraphic:
            [self handleUploadGraphicWithModel:model];
            break;
            
        case ZegoOperationEventFlagTypeUploadPicByURL:
            [self handleUploadPictureByURL:model];
            break;
            
        case ZegoOperationEventFlagTypeDocsCacheFile:
            [self cacheFileID:model.value];
            break;
        case ZegoOperationEventFlagTypeDocsQueryCache:
            [self queryFileCached:model.value];
            break;
        case ZegoOperationEventFlagTypeUploadPicByAlbum:
            [self handleUploadPictureByAlbum:model];
            break;;
        case ZegoOperationEventFlagTypeUploadLog:
            [ZegoProgessHUD showTipMessage:@"日志已上传"];
            [ZegoRoomSeviceCenter uploadLog];
            break;
        case ZegoOperationEventFlagTypeLocalBackground:
            [self handleLocalBackground];
            break;
        case ZegoOperationEventFlagTypeOnlineBackground:
            [self handleOnlineBackgroundWithModel:model];
            break;
        case ZegoOperationEventFlagTypeCleanBackground:
            [[ZegoBoardOperationManager shareManager] cleanBackgroundImage];
            break;
        default:
            break;
    }
}

- (void)handleFunctionPannelEventWithModel:(ZegoCommonCellModel *)model {
    DLog(@"EventHandler>>> handleFunctionPannelEventWithModel:%lu",(unsigned long)model.eventNumber);
    switch (model.eventNumber) {
        case ZegoOperationEventFlagTypeClearModel:
            [self handleEventClearModel:model];
            break;
        
        case ZegoOperationEventFlagTypeDocsUpload:
            [self handleFileOperationWithModel:model];
            break;
            
        case ZegoOperationEventFlagTypeUploadPicByAlbum:
            [self handleUploadPictureByAlbum:model];
            break;
            
        case ZegoOperationEventFlagTypePreview:
            [self handleEventPreview:model];
            break;
            
            
        default:
            break;
    }
}

- (void)handleEventPreview:(ZegoCommonCellModel *)model {
    NSInteger index = [model.value integerValue];
    DLog(@"EventHandler>>> handleEventPreview:%ld",(long)index);
    if (index == 0) {
        [[ZegoBoardOperationManager shareManager] getThumbnailUrlList];
    } else {
        [[ZegoBoardOperationManager shareManager] showPreview];
    }
    
}

- (void)handleEventFontType:(ZegoCommonCellModel *)model {
    ZegoCellOptionModel *optionBoldModel = model.options.firstObject;
    BOOL boldEnable = [optionBoldModel.value boolValue];
    
    ZegoCellOptionModel *optionItalicModel = model.options.lastObject;
    BOOL italicEnable = [optionItalicModel.value boolValue];

    DLog(@"EventHandler>>> handleEventFontType,bold:%@,italic:%@",boldEnable?@"YES":@"NO",italicEnable?@"YES":@"NO");
    [[ZegoBoardOperationManager shareManager] setupEnableBoldFont:boldEnable];
    [[ZegoBoardOperationManager shareManager] setupEnableItalicFont:italicEnable];
}

- (void)handleEventOperationMode:(ZegoCommonCellModel *)model {
    NSInteger currentMode = 0;
    for (int i = 0; i < model.options.count; i++) {
        ZegoCellOptionModel *optionModel = model.options[i];
        BOOL result = [optionModel.value boolValue];
        if (result) {
            currentMode = currentMode | (1 << i);
        }
    }
//    bool result1 = (currentMode & ZegoWhiteboardOperationModeDraw);
//    bool result2 = (currentMode & ZegoWhiteboardOperationModeScroll);
//    if ( result1 == result2) {
//        [ZegoProgessHUD showTipMessage:@"不可以同时设置scroll和draw 模式"];
//        return;
//    }
    DLog(@"EventHandler>>> handleEventOperationMode:%ld",(long)currentMode);

    [[ZegoBoardOperationManager shareManager] setupWhiteboardOperationMode:currentMode];
}

- (void)handleFileOperationWithModel:(ZegoCommonCellModel *)model {
    NSInteger index = [model.value integerValue];
    DLog(@"EventHandler>>> handleFileOperationWithModel,index:%ld",(long)index);
    switch (index) {
        case 0:
            [self uploadFileWithRenderType:ZegoDocsViewRenderTypeVectorAndIMG];
            break;
            
        case 1:
            [self uploadFileWithRenderType:ZegoDocsViewRenderTypeDynamicPPTH5];
            break;
            
        case 2:
            [self cancelUploadFileSeq:0];
            break;
            
        case 3:
            [[ZegoBoardOperationManager shareManager] clearFileCache];
            break;
            
        default:
            break;
    }
}

//添加指定位置的指定文本
- (void)handleEventSpecialTextGraphic:(ZegoCommonCellModel *)model {
    if ([ZegoBoardOperationManager shareManager].toolType != ZegoWhiteboardViewToolText) {
        [ZegoProgessHUD showTipMessage:@"仅在文本模式下可用"];
        return;
    }
    ZegoCellOptionModel *textModel = model.options.firstObject;
    ZegoCellOptionModel *postionModel = model.options.lastObject;
    NSString *positionString = postionModel.value;
    NSArray *positionArray = [positionString componentsSeparatedByString:@"."];
    CGPoint position =  CGPointMake([positionArray.firstObject floatValue], [positionArray.lastObject floatValue]);
    DLog(@"EventHandler>>> handleEventSpecialTextGraphic:%@ position:%@",textModel.value,NSStringFromCGPoint(position));
    [[ZegoBoardOperationManager shareManager] addGraphicWithText:textModel.value postion:position];
}

//清除模式事件处理
- (void)handleEventClearModel:(ZegoCommonCellModel *)model {
    NSInteger index = [model.value integerValue];
    DLog(@"EventHandler>>> handleEventClearModel,index:%ld",(long)index);
    switch (index) {
        case 0:
            [[ZegoBoardOperationManager shareManager] clearAllGraphic];
            break;
        
        case 1:
            [[ZegoBoardOperationManager shareManager] undoGraphic];
            break;
            
        case 2:
            [[ZegoBoardOperationManager shareManager] redoGraphic];
            break;
            
        case 3:
            [[ZegoBoardOperationManager shareManager] clearCurrentPage];
            break;
            
        case 4:
            [[ZegoBoardOperationManager shareManager] clearCurrentSelected];
            break;
            
        default:
            break;
    }
}

- (void)handleUploadGraphicWithModel:(ZegoCommonCellModel *)model {
    NSInteger index = 9;
    //获取自定义图形 url 信息
    NSString *urlString = model.options.firstObject.value;
    NSString *fileName = [urlString lastPathComponent];
    [ZegoProgessHUD showTipMessage:@"正在下载自定义图形"];
    DLog(@"EventHandler>>> handleUploadGraphicWithModel,url:%@",urlString);
    [[ZegoBoardOperationManager shareManager] setCustomImageGraphicWithURLString:urlString complete:^(int error) {
        if (error != 0) {
            [ZegoProgessHUD showTipMessageWithErrorCode:error];
            return;
        }
        [ZegoProgessHUD showTipMessage:@"自定义图形下载完成"];
        //添加数据到 plist 文件中
        //获取 plist 文件路径
        NSString *sandPath = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES).firstObject;
        NSString *path = [NSString stringWithFormat:@"%@/%@", sandPath, @"ZegoDrawPannel.plist"];
        NSMutableArray *plistArray = [[NSMutableArray alloc] initWithContentsOfFile:path];
        // 获取选择图形选项位置, hardcode 9
        NSMutableDictionary *selectShape = ((NSDictionary *)plistArray[index]).mutableCopy;
        NSArray *shapes = selectShape[@"options"];
        NSDictionary *shape = @{
            @"title": fileName,
            @"value": urlString,
        };
        NSMutableArray *mShapes = [[NSMutableArray alloc] initWithObjects:shape, nil];
        [mShapes addObjectsFromArray:shapes];
        
        // 写回
        selectShape[@"options"] = mShapes.copy;
        plistArray[index] = selectShape.copy;
        __unused BOOL ret = [plistArray writeToFile:path atomically:YES];
        [[NSNotificationCenter defaultCenter] postNotificationName:@"reloadPlist" object:nil];
    }];
    
    
}

- (void)handleSelectGraphicWithModel:(ZegoCommonCellModel *)model {
    NSInteger index = ((NSNumber *)model.value).integerValue;
    NSString *urlString = model.options[index].value;
    DLog(@"EventHandler>>> handleSelectGraphicWithModel,index:%ld,url:%@",(long)index,urlString);
    [[ZegoBoardOperationManager shareManager] setCustomImageGraphicWithURLString:urlString complete:^(int error) {
        
    }];
}

- (void)handleUploadPictureByURL:(ZegoCommonCellModel *)model {
    ZegoCellOptionModel *urlModel = model.options.firstObject;
    ZegoCellOptionModel *optionModel = model.options.lastObject;
    NSArray *positionArray = [optionModel.value componentsSeparatedByString:@"."];
    CGPoint position =  CGPointMake([positionArray.firstObject floatValue], [positionArray.lastObject floatValue]);
    self.imagePoint = position;
    DLog(@"EventHandler>>> handleUploadPictureByURL,url:%@,position:%@",urlModel.value,NSStringFromCGPoint(position));
    [self uploadPicture:urlModel.value];
}

- (void)handleUploadPictureByAlbum:(ZegoCommonCellModel *)model {
    ZegoCellOptionModel *postionModel = model.options.firstObject;
    NSString *positionString = postionModel.value;
    NSArray *positionArray = [positionString componentsSeparatedByString:@"."];
    CGPoint position =  CGPointMake([positionArray.firstObject floatValue], [positionArray.lastObject floatValue]);
    self.imagePoint = position;
    __weak typeof(self) weakSelf = self;
    self.documentSelectorBlock = ^(NSURL *fileUrl) {
        __strong typeof(weakSelf) strongSelf = weakSelf;
        [strongSelf uploadPicture:fileUrl.path];
    };
    DLog(@"EventHandler>>> handleUploadPictureByAlbum,position:%@",NSStringFromCGPoint(position));
    [self openDocumentSelector];
}

- (void)uploadPicture:(NSString *)filePath {
    [[NSNotificationCenter defaultCenter]postNotificationName:@"addImage" object:nil userInfo:@{@"file":filePath,@"point":@(self.imagePoint)}];
}

- (void)uploadBackgroundPicture:(NSString *)filePath {
    if (!_backgroundMode) {
        _backgroundMode = @(0);
    }
    [[NSNotificationCenter defaultCenter] postNotificationName:@"setBackgroundImage" object:nil userInfo:@{@"file":filePath, @"mode":_backgroundMode}];
}

- (void)handleBackgroundModeWithModel:(ZegoCommonCellModel *)model {
    _backgroundMode = model.value;
}

- (void)handlePresetBackgroundWithModel:(ZegoCommonCellModel *)model {
    NSInteger index = ((NSNumber *)model.value).integerValue;
    NSString *path = model.options[index].value;
    [self uploadBackgroundPicture:path];
}

- (void)handleOnlineBackgroundWithModel:(ZegoCommonCellModel *)model {
    ZegoCellOptionModel *urlModel = model.options.firstObject;
    [self uploadBackgroundPicture:urlModel.value];
}

- (void)handleLocalBackground {
    __weak typeof(self) weakSelf = self;
    self.documentSelectorBlock = ^(NSURL *fileUrl) {
        __strong typeof(weakSelf) strongSelf = weakSelf;
        [strongSelf uploadBackgroundPicture:fileUrl.path];
    };
    [self openDocumentSelector];
}

//上传文件
- (void)uploadFileWithRenderType:(ZegoDocsViewRenderType)renderType {
    
    self.currentRenderType = renderType;
    __weak typeof(self) weakSelf = self;
    self.documentSelectorBlock = ^(NSURL *fileUrl) {
        __strong typeof(weakSelf) strongSelf = weakSelf;
        [strongSelf uploadFileWithUrl:fileUrl renderType:strongSelf.currentRenderType];
    };
    DLog(@"EventHandler>>> uploadFileWithRenderType:%lu",(unsigned long)renderType);
    [self openDocumentSelector];
}
    
- (void)openDocumentSelector {
    NSArray *documentTypes = @[@"public.content",
                               @"com.adobe.pdf",
                               @"com.microsoft.word.doc",
                               @"com.microsoft.excel.xls",
                               @"com.microsoft.powerpoint.ppt",
                               @"public.image"];
    UIDocumentPickerViewController *documentPicker = [[UIDocumentPickerViewController alloc] initWithDocumentTypes:documentTypes inMode:UIDocumentPickerModeOpen];
    documentPicker.delegate = self;
    if (@available(iOS 11.0, *)) {
        documentPicker.allowsMultipleSelection = NO;
    }
    documentPicker.modalPresentationStyle = UIModalPresentationFormSheet;
    UINavigationController *navVC =  (UINavigationController *)[UIApplication sharedApplication].keyWindow.rootViewController;
    [navVC.topViewController presentViewController:documentPicker animated:YES completion:nil];
}

- (void)uploadFileWithUrl:(NSURL *)url renderType:(ZegoDocsViewRenderType)type
{
    [url startAccessingSecurityScopedResource];
    __weak typeof(self) weakSelf = self;
    ZegoProgessHUD *hudView = [[ZegoProgessHUD alloc] initWithTitle:@"上传中..." cancelBlock:^{
        __strong typeof(weakSelf) strongSelf = weakSelf;
        [strongSelf cancelUploadFileSeq:strongSelf.currentUploadSeq ];
    }];
    self.currentUploadSeq = [[ZegoBoardOperationManager shareManager] uploadFile:url.path renderType:type completionBlock:^(ZegoDocsViewUploadState state, ZegoDocsViewError errorCode, NSDictionary *infoDictionary) {
        if (errorCode == ZegoDocsViewSuccess) {
            if (state == ZegoDocsViewUploadStateUpload) {
                NSNumber * upload_percent = infoDictionary[UPLOAD_PERCENT];
                [hudView updateProgress:upload_percent.floatValue];
                DLog(@"EventHandler>>> uploadFile uploading,upload_percent:%f",upload_percent.floatValue);
                
            } else if (state == ZegoDocsViewUploadStateConvert){
                [ZegoProgessHUD showTipMessage:@"转码完成"];
                NSString *fileID = infoDictionary[UPLOAD_FILEID];
                [[ZegoBoardOperationManager shareManager] addNewWhiteboardWithName:@"" fileID:fileID];
                self.currentFileID = fileID;
                DLog(@"EventHandler>>> uploadFile finished,fileID:%@",fileID);
            }
        } else {
            [ZegoProgessHUD showTipMessage:[NSString stringWithFormat:@"上传失败: %zd", errorCode]];
            DLog(@"EventHandler>>> uploadFile failed,error:%lu",(unsigned long)errorCode);
        }
    }];
    DLog(@"EventHandler>>> uploadFileWithUrl, url: %@ ,seq:%u", [url path],self.currentUploadSeq);
    [url stopAccessingSecurityScopedResource];
}

- (void)cancelUploadFileSeq:(ZegoSeq)seq {
    DLog(@"EventHandler>>> cancelUploadFileSeq:%d",seq);
    [[ZegoBoardOperationManager shareManager] cancelUploadFileSeq:seq completionBlock:^(ZegoDocsViewError errorCode) {
        NSString *msg;
        if (errorCode == 0) {
            msg = @"已取消";
        } else {
            msg = [NSString stringWithFormat:@"取消失败: %zd", errorCode];
        }
        [ZegoProgessHUD showTipMessage:msg];
        DLog(@"EventHandler>>> cancelUploadFileSeq:%d error:%lu",seq,(unsigned long)errorCode);
    }];
}

- (void)cacheFileID:(NSString *)fileID {
    DLog(@"EventHandler>>> cacheFileID:%@",fileID);
    __weak typeof(self) weakSelf = self;
    ZegoProgessHUD *hudView = [[ZegoProgessHUD alloc] initWithTitle:@"下载中..." cancelBlock:^{
        __strong typeof(weakSelf) strongSelf = weakSelf;
        [strongSelf cancelCacheFileSeq:strongSelf.currentCacheSeq];
    }];
    if (fileID.length > 0) {
        self.currentCacheSeq = [[ZegoBoardOperationManager shareManager] cacheFileWithFileId:fileID completionBlock:^(ZegoDocsViewCacheState state, ZegoDocsViewError errorCode, NSDictionary *infoDictionary) {
            if (errorCode == 0) {
                if (state == ZegoDocsViewCacheStateCaching) {
                    NSNumber * download_percent = infoDictionary[CACHE_PERCENT];
                    [hudView updateProgress:download_percent.floatValue];
                    DLog(@"EventHandler>>> cacheFile downloading,download_percent:%f",download_percent.floatValue);
                    
                } else if (state == ZegoDocsViewCacheStateCached){
                    [ZegoProgessHUD showTipMessage:@"下载完成"];
                    [[ZegoBoardOperationManager shareManager] addNewWhiteboardWithName:@"" fileID:fileID];
                    DLog(@"EventHandler>>> cacheFile finished,fileID:%@",fileID);
                }
            } else {
                [ZegoProgessHUD showTipMessage:[NSString stringWithFormat:@"下载失败: %zd", errorCode]];
                DLog(@"EventHandler>>> cacheFile failed,error:%lu",(unsigned long)errorCode);
            }
        }];
    }else {
        [ZegoProgessHUD showTipMessage:@"请输入 FileID"];
    }
}

- (void)cancelCacheFileSeq:(ZegoSeq)seq {
    DLog(@"EventHandler>>> cancelCacheFileSeq:%d",seq);
    if (!seq) {
        seq = self.currentCacheSeq;
    }
    [[ZegoBoardOperationManager shareManager] cancelCacheFileSeq:seq completionBlock:^(ZegoDocsViewError errorCode) {
        if (errorCode == 0) {
            [ZegoProgessHUD showTipMessage:@"取消完成"];
        } else {
            [ZegoProgessHUD showTipMessage:@"取消失败"];
        }
        DLog(@"EventHandler>>> cancelCacheFileSeq:%d  error:%lu",seq,(unsigned long)errorCode);
    }];
}

- (void)queryFileCached:(NSString *)fileID {
    DLog(@"EventHandler>>> queryFileCached:%@", fileID);
    if (fileID.length > 0) {
        [[ZegoBoardOperationManager shareManager] queryFileCachedWithFileId:fileID completionBlock:^(ZegoDocsViewError errorCode, BOOL fileExist) {
            NSString *msg;
            if (errorCode == 0) {
                NSString *result = fileExist ? @"已缓存" : @"未缓存";
                msg = [NSString stringWithFormat:@"查询成功, 文件%@", result];
            }else {
                msg = [NSString stringWithFormat:@"查询失败: %zd", errorCode];
            }
            [ZegoProgessHUD showTipMessage:msg];
        }];
    }else {
        [ZegoProgessHUD showTipMessage:@"请输入 FileID"];
    }
}

#pragma mark - UIDocumentPickerDelegate
- (void)documentPicker:(UIDocumentPickerViewController *)controller didPickDocumentsAtURLs:(NSArray <NSURL *>*)urls NS_AVAILABLE_IOS(11_0)
{
    if([urls isKindOfClass:[NSArray class]])
        [self documentPicker:controller didPickDocumentAtURL:urls.firstObject];
}

// 选中icloud里的pdf文件 iOS 8-11
- (void)documentPicker:(UIDocumentPickerViewController *)controller didPickDocumentAtURL:(NSURL *)url
{
    BOOL fileUrlAuthozied = [url startAccessingSecurityScopedResource];
    if(fileUrlAuthozied){
        NSFileCoordinator *fileCoordinator = [[NSFileCoordinator alloc] init];
        NSError *error;
        __weak typeof(self) weakSelf = self;
        [fileCoordinator coordinateReadingItemAtURL:url options:0 error:&error byAccessor:^(NSURL *newURL) {
            __strong typeof(weakSelf) strongSelf = weakSelf;
            if (strongSelf.documentSelectorBlock) {
                strongSelf.documentSelectorBlock(newURL);
                strongSelf.documentSelectorBlock = nil;
            }
        }];
        if (error) {
            [url stopAccessingSecurityScopedResource];
        }
    } else {
        NSLog(@"--- no permission ---");
    }
}

- (void)documentPickerWasCancelled:(UIDocumentPickerViewController *)controller {
    NSLog(@"--- cancel ---");
}


@end
