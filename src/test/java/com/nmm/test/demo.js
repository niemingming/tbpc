Hub = {};
Hub.config = {
    config: {},
    get: function(key) {
        if (key in this.config) {
            return this.config[key];
        } else {
            return null;
        }
    },
    set: function(key, val) {
        this.config[key] = val;
    }
};

Hub.config.set('sku', {
    valCartInfo      : {
        itemId : '570316136094',
        cartUrl: '//cart.taobao.com/cart.htm'
    },
    apiRelateMarket  : '//tui.taobao.com/recommend?appid=16&count=4&itemid=570316136094',
    apiAddCart       : '//cart.taobao.com/add_cart_item.htm?item_id=570316136094',
    apiInsurance     : '',
    wholeSibUrl      : '//detailskip.taobao.com/service/getData/1/p1/item/detail/sib.htm?itemId=570316136094&sellerId=677255500&modules=dynStock,qrcode,viewer,price,duty,xmpPromotion,delivery,activity,fqg,zjys,couponActivity,soldQuantity,originalPrice,tradeContract',
    areaLimit        : '',
    bigGroupUrl      : '',
    valPostFee       : '',
    coupon           : {
        couponApi         : '//detailskip.taobao.com/json/activity.htm?itemId=570316136094&sellerId=677255500',
        couponWidgetDomain: '//assets.alicdn.com',
        cbUrl             : '/cross.htm?type=weibo'
    },
    valItemInfo      : {

        defSelected: -1,
        skuMap     : {";1627207:2206589265;":{"price":"22.90","stock":"2","skuId":"3679729361760","oversold":false},";1627207:4486299;":{"price":"75.90","stock":"2","skuId":"3679729361768","oversold":false},";1627207:980031237;":{"price":"19.90","stock":"2","skuId":"3679729361771","oversold":false},";1627207:370381785;":{"price":"26.90","stock":"2","skuId":"3679729361766","oversold":false},";1627207:2207570802;":{"price":"42.90","stock":"2","skuId":"3679729361761","oversold":false},";1627207:1150247465;":{"price":"39.90","stock":"2","skuId":"3679729361749","oversold":false},";1627207:2212466895;":{"price":"19.90","stock":"2","skuId":"3679729361762","oversold":false},";1627207:1641173753;":{"price":"24.90","stock":"2","skuId":"3679729361757","oversold":false},";1627207:980031238;":{"price":"19.90","stock":"2","skuId":"3679729361772","oversold":false},";1627207:1270670659;":{"price":"49.90","stock":"2","skuId":"3679729361752","oversold":false},";1627207:1503218164;":{"price":"24.90","stock":"2","skuId":"3679729361754","oversold":false},";1627207:2212466896;":{"price":"29.90","stock":"2","skuId":"3679729361763","oversold":false},";1627207:916995191;":{"price":"22.90","stock":"2","skuId":"3679729361770","oversold":false},";1627207:2212466897;":{"price":"32.90","stock":"2","skuId":"3679729361764","oversold":false},";1627207:761125975;":{"price":"32.90","stock":"2","skuId":"3679729361769","oversold":false},";1627207:1240216589;":{"price":"19.90","stock":"2","skuId":"3679729361750","oversold":false},";1627207:399487786;":{"price":"25.90","stock":"2","skuId":"3679729361767","oversold":false},";1627207:259699464;":{"price":"59.90","stock":"2","skuId":"3679729361765","oversold":false},";1627207:1839532555;":{"price":"62.90","stock":"2","skuId":"3679729361759","oversold":false},";1627207:1839532553;":{"price":"49.90","stock":"2","skuId":"3679729361758","oversold":false},";1627207:1417642902;":{"price":"29.90","stock":"2","skuId":"3679729361753","oversold":false},";1627207:1605903609;":{"price":"29.90","stock":"2","skuId":"3679729361756","oversold":false},";1627207:1504509881;":{"price":"39.90","stock":"2","skuId":"3679729361755","oversold":false},";1627207:1256678004;":{"price":"39.90","stock":"2","skuId":"3679729361751","oversold":false}}
        ,propertyMemoMap: {"1627207:980031238":"扭扭虫+小绕珠+响板+木蛋","1627207:399487786":"扭扭虫+绕珠+敲琴","1627207:980031237":"彩虹塔+小绕珠+响板+木蛋","1627207:1504509881":"四套柱+趣味动物绕珠+彩虹塔","1627207:1839532555":"F套餐豪华四件套","1627207:1417642902":"趣味动物绕珠+敲琴","1627207:1503218164":"新三档海洋生物绕珠","1627207:4486299":"豪华五件套","1627207:1839532553":"D套餐豪华三件套","1627207:1256678004":"G套餐四件套","1627207:1270670659":"H套餐四件套","1627207:2207570802":"趣味动物绕珠+甲壳虫套柱+扭扭虫","1627207:2206589265":"新三档水果大绕珠","1627207:1240216589":"双档趣味动物大绕珠","1627207:2212466896":"敲琴+小绕珠+扭扭虫+彩虹塔","1627207:2212466895":"扭扭虫+彩虹塔+绕珠","1627207:1605903609":"四套柱+趣味动物大绕珠","1627207:916995191":"趣味动物绕珠（送扭扭虫）","1627207:2212466897":"趣味动物绕珠+扭扭虫+敲琴","1627207:761125975":"三档榉木动物大绕珠+木蛋","1627207:1641173753":"新三档农场动物绕珠","1627207:1150247465":"趣味动物绕珠+彩虹塔+敲琴","1627207:259699464":"E套餐豪华四件套","1627207:370381785":"小绕珠+手敲琴+彩虹塔"}


    }
});

Hub.config.set('desc', {
    dummy       : false,
    apiImgInfo  : '//tds.alicdn.com/json/item_imgs.htm?t=TB1LpCmtSBYBeNjy0FeXXbnmFXa&sid=677255500&id=570316136094&s=f38b2410a68ac20855b7fee6bbd5f64d&v=2&m=1',
    similarItems: {
        api           : '//tds.alicdn.com/recommended_same_type_items.htm?v=1',
        rstShopId     : '482284953',
        rstItemId     : '570316136094',
        rstdk         : 0,
        rstShopcatlist: ''
    }
});


Hub.config.set('async_dc', {
    newDc : true,
    api   : '//hdc1.alicdn.com/asyn.htm?userId=677255500&pageId=1580343706&v=2014'
});


Hub.config.set('support', {
    url : ''
});

Hub.config.set('async_sys', {
    api: '//item.taobao.com/asyn.htm?g=sys&v=2'
});




