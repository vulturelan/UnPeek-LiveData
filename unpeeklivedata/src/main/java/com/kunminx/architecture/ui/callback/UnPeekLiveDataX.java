package com.kunminx.architecture.ui.callback;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

/**
 * TODO：UnPeekLiveData 的存在是为了在 "重回二级页面" 的场景下，解决 "数据倒灌" 的问题。
 * 对 "数据倒灌" 的状况不理解的小伙伴，可参考《jetpack MVVM 精讲》的解析
 * <p>
 * https://juejin.im/post/5dafc49b6fb9a04e17209922
 * <p>
 * 本类参考了官方 SingleEventLive 的非入侵设计，
 * <p>
 * TODO：并创新性地引入了 "延迟清空消息" 的设计，
 * 如此可确保：
 * 1.一条消息能被多个观察者消费
 * 2.延迟期结束，不再能够收到旧消息的推送
 * 3.并且旧消息在延迟期结束时能从内存中释放，避免内存溢出等问题
 * 4.让非入侵设计成为可能，遵循开闭原则
 * <p>
 * TODO：增加一层 ProtectedUnPeekLiveData，
 * 用于限制从 Activity/Fragment 篡改来自 "数据层" 的数据，数据层的数据务必通过唯一可信源来分发，
 * 如果这样说还不理解，详见：
 * https://xiaozhuanlan.com/topic/6719328450 和 https://xiaozhuanlan.com/topic/0168753249
 * <p>
 * Create by KunMinX at 2020/7/21
 */
public class UnPeekLiveDataX<T> extends ProtectedUnPeekLiveDataX<T> {

    @Override
    public void setValue(T value) {
        super.setValue(value);
    }

    @Override
    public void postValue(T value) {
        super.postValue(value);
    }

    /**
     * 请不要在 UnPeekLiveData 中使用 observe 方法，
     * 取而代之的是在 Activity 和 fragment 中分别使用 observeActivity 和 observeFragment 来观察。
     * <p>
     * 2020.10.15 背景缘由：
     * UnPeekLiveData 通过 ViewModelStore 来在各种场景下（如旋屏后）确定订阅者的唯一性和消息的消费状况，
     * 因而在 Activity 和 fragment 对 LifecycleOwner 的使用存在差异的现状下，
     * 我们采取注入局部变量的方式，来获取 store 和 owner。
     *
     * @param owner
     * @param observer
     */
    @Override
    public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<? super T> observer) {
        throw new IllegalArgumentException("Taking into account the normal permission of preventing backflow logic, " +
                " do not use observeForever to communicate between pages");
    }

    /**
     * 请不要在 UnPeekLiveData 中使用 observeForever 方法。
     * <p>
     * 2020.8.1 背景缘由：
     * UnPeekLiveData 主要用于表现层的 页面转场 和 页面间通信 场景下的非粘性消息分发，
     * 出于生命周期安全等因素的考虑，不建议使用 observeForever 方法，
     * <p>
     * 对于数据层的工作，如有需要，可结合实际场景使用 RxJava 或 kotlin flow。
     *
     * @param observer
     */
    @Override
    public void observeForever(@NonNull Observer<? super T> observer) {
        throw new IllegalArgumentException("Considering avoid lifecycle security issues," +
                " do not use observeForever for communication between pages.");
    }

    public static class Builder<T> {

        /**
         * 是否允许传入 null value
         */
        private boolean isAllowNullValue;

        public Builder<T> setAllowNullValue(boolean allowNullValue) {
            this.isAllowNullValue = allowNullValue;
            return this;
        }

        public UnPeekLiveDataX<T> create() {
            UnPeekLiveDataX<T> liveData = new UnPeekLiveDataX<>();
            liveData.isAllowNullValue = this.isAllowNullValue;
            return liveData;
        }
    }
}