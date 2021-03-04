//
//  ZegoCommonCellModel.m
//  ZegoWhiteboardExample
//
//  Created by Xuyang Nie on 2020/11/26.
//

#import "ZegoCommonCellModel.h"
#import <NSObject+YYModel.h>
@implementation ZegoCommonCellModel
- (instancetype)initWithTitle:(NSString *)title type:(ZegoSettingTableViewCellType)type options:(nullable NSArray *)options value:(nullable id)value {
    if (self = [super init]) {
        _title = title;
        _type = type;
        _options = options;
        _value = value;
        _tag = @"";
        _placeholder = @"";
        _extra = @{};
        _cellHeight = 0;
    }
    return self;
}

+ (NSDictionary *)modelContainerPropertyGenericClass {
    return @{
        @"options" : [ZegoCellOptionModel class]
        
    };
}
@end

@implementation ZegoCellOptionModel



@end
