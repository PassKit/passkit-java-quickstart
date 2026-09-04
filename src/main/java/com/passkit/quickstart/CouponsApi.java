package com.passkit.quickstart;

import com.google.protobuf.Empty;
import com.passkit.grpc.CommonObjects;
import com.passkit.grpc.Filter;
import com.passkit.grpc.Personal;
import com.passkit.grpc.Reporting;
import com.passkit.grpc.SingleUseCoupons.Campaign;
import com.passkit.grpc.SingleUseCoupons.CouponOuterClass;
import com.passkit.grpc.SingleUseCoupons.Offer;
import com.passkit.grpc.SingleUseCoupons.SingleUseCouponsGrpc;
import io.grpc.Channel;
import io.grpc.stub.StreamObserver;

import java.util.Iterator;

/** Typed coupon facade combining the SDK's blocking operations and update streams. */
public final class CouponsApi {
    private final SingleUseCouponsGrpc.SingleUseCouponsBlockingStub blocking;
    private final SingleUseCouponsGrpc.SingleUseCouponsStub async;

    CouponsApi(Channel channel) {
        blocking = SingleUseCouponsGrpc.newBlockingStub(channel);
        async = SingleUseCouponsGrpc.newStub(channel);
    }

    public CommonObjects.Id createCouponCampaign(Campaign.CouponCampaign value) { return blocking.createCouponCampaign(value); }
    public Campaign.CouponCampaign getCouponCampaign(CommonObjects.Id value) { return blocking.getCouponCampaign(value); }
    public Campaign.CouponCampaign updateCouponCampaign(Campaign.CouponCampaign value) { return blocking.updateCouponCampaign(value); }
    public Empty deleteCouponCampaign(CommonObjects.Id value) { return blocking.deleteCouponCampaign(value); }
    public Iterator<Campaign.CouponCampaign> listCouponCampaigns(Filter.Filters value) { return blocking.listCouponCampaigns(value); }
    public CommonObjects.Id createCouponOffer(Offer.CouponOffer value) { return blocking.createCouponOffer(value); }
    public Offer.CouponOffer getCouponOffer(CommonObjects.Id value) { return blocking.getCouponOffer(value); }
    public Offer.CouponOffer updateCouponOffer(Offer.CouponOffer value) { return blocking.updateCouponOffer(value); }
    public Empty deleteCouponOffer(CommonObjects.Id value) { return blocking.deleteCouponOffer(value); }
    public Iterator<Offer.CouponOffer> listCouponOffers(Offer.CouponOffersListRequest value) { return blocking.listCouponOffers(value); }
    public CommonObjects.Id createCoupon(CouponOuterClass.Coupon value) { return blocking.createCoupon(value); }
    public CouponOuterClass.Coupon getCouponById(CommonObjects.Id value) { return blocking.getCouponById(value); }
    public CouponOuterClass.Coupon getCouponByExternalId(CouponOuterClass.ExternalIdRequest value) { return blocking.getCouponByExternalId(value); }
    public CommonObjects.Id updateCoupon(CouponOuterClass.Coupon value) { return blocking.updateCoupon(value); }
    public CommonObjects.Id updateCouponExternalId(CouponOuterClass.CouponNewExternalIdRequest value) { return blocking.updateCouponExternalId(value); }
    public CommonObjects.Id patchPerson(Personal.PersonRequest value) { return blocking.patchPerson(value); }
    public CommonObjects.Id redeemCoupon(CouponOuterClass.Coupon value) { return blocking.redeemCoupon(value); }
    public Empty voidCoupon(CouponOuterClass.Coupon value) { return blocking.voidCoupon(value); }
    public Iterator<CouponOuterClass.Coupon> listCouponsByCouponCampaign(CouponOuterClass.ListRequest value) { return blocking.listCouponsByCouponCampaign(value); }
    public CommonObjects.Count countCouponsByCouponCampaign(CouponOuterClass.ListRequest value) { return blocking.countCouponsByCouponCampaign(value); }
    public Campaign.CouponCampaignAnalyticsResponse getAnalytics(Reporting.AnalyticsRequest value) { return blocking.getAnalytics(value); }
    public CommonObjects.Strings getMetaKeysForCampaign(CommonObjects.Id value) { return blocking.getMetaKeysForCampaign(value); }
    public StreamObserver<CouponOuterClass.Coupon> streamCouponUpdates(StreamObserver<CommonObjects.Id> responses) {
        return async.streamCouponUpdates(responses);
    }
    public StreamObserver<CouponOuterClass.Coupon> streamCouponRedemptions(StreamObserver<CommonObjects.Id> responses) {
        return async.streamCouponRedemptions(responses);
    }
}
