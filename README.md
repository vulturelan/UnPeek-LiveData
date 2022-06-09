![](https://images.xiaozhuanlan.com/photo/2022/11110ff4abb5e546b05031baee3ea97e.png)

## 前言

大家好，我是[《Jetpack MVVM Best Practice》](https://github.com/KunMinX/Jetpack-MVVM-Best-Practice)作者 KunMinX。

今天提到 “数据倒灌” 一词，缘于我为方便理解和记忆 **“再入二级页面时收到旧数据推送” 情况**，而在 2019 年 **自创并于网上传播的此现象概括**。

它主要发生于：SharedViewModel + LiveData 组合实现页面通信场景。

&nbsp;

## 背景

由于本文目标是介绍官方 Demo 现有解决方案缺陷，及经过 2 年迭代趋于完美解决方案，

所以我们假设在座诸位对最基本 “背景缘由” 有一定了解，知道：

> 为何 LiveData 默认被设计为粘性事件

> 为何 [官方文档 ](https://developer.android.google.cn/topic/libraries/architecture/viewmodel#sharing) 推荐使用 SharedViewModel + LiveData（文档没明说，但事实上包含三个关键背景缘由）

> 乃至为何存在 “数据倒灌” 现象

> 及为何 “页面通信” 场景下，不用静态单例或 EventBus、LiveDataBus 等消息总线，

如对这些前置知识也尚无体会，可结合个人兴趣前往[《LiveData 数据倒灌 背景缘由全貌 独家解析》](https://xiaozhuanlan.com/topic/6719328450)查阅，此处不再累述。

&nbsp;

## 现有解决方案及各自缺陷

在[《Jetpack MVVM 精讲》](https://juejin.im/post/5dafc49b6fb9a04e17209922)中我们分别提到 **Event 事件包装器、反射方式、SingleLiveEvent** 三种方式解决 “数据倒灌” 问题。它们分别来自文中提到的[外网](https://medium.com/androiddevelopers/livedata-with-snackbar-navigation-and-other-events-the-singleliveevent-case-ac2622673150)、[美团](https://tech.meituan.com/2018/07/26/android-livedatabus.html)文章，和[官方最新 demo](https://github.com/android/architecture-samples/blob/dev-todo-mvvm-live/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/SingleLiveEvent.java)。

但正如[《Jetpack MVVM 精讲》](https://juejin.im/post/5dafc49b6fb9a04e17209922)所述，它们分别存在如下问题：

> **Event 事件包装器：**

对于多观察者情况，只允许第一个观察者消费，这不符合现实需求；

而且手写 Event 事件包装器，在 Java 中存在 Null 安全一致性问题。


> **反射干预 Version 方式：**

存在延迟，无法用于对实时性有要求场景；

且数据会随着 SharedViewModel 长久滞留内存中得不到释放。


> **官方最新 demo SingleLiveEvent：**

是对 Event 事件包装器 “一致性问题” 改进，但未解决 “多观察者消费” 问题；

且额外引入 “消息未能从内存中释放” 问题。

&nbsp;

## UnPeek-LiveData 特点

> 1.一条消息能被多个观察者消费（since v1.0）

> 2.消息被所有观察者消费完毕后才开始阻止倒灌（since v4.0）

> 3.可通过 clear 方法手动将消息从内存中移除（since v4.0）

> 4.让非入侵设计成为可能，遵循开闭原则（since v3.0）

> 5.基于 "访问权限控制" 支持 "读写分离"，遵循 “唯一可信源” 消息分发理念（since v2.0，详见 ProtectedUnPeekLiveData）

且 UnPeekLiveData 提供构造器模式，后续可通过构造器组装适合自己业务场景 UnPeekLiveData。

```java
UnPeekLiveData<Moment> test =
  new UnPeekLiveData.Builder<Moment>()
    .setAllowNullValue(false)
    .create();
```

&nbsp;

## Maven 依赖

```groovy
implementation 'com.kunminx.arch:unpeek-livedata:7.4.1'
```

> 温馨提示：
>
> 1.上述 implementation 命名，我们已从 `archi` 改为 `arch`，请注意修改，
>
> 2.鉴于 Jcenter 关闭，我们已将仓库迁移至 Maven Central，请自行在根目录 build.gradle 添加 `mavenCentral()`。

&nbsp;

## Thanks

PS：感谢近期 [hegaojian](https://github.com/hegaojian)、Angki、Flynn、[Joker_Wan](https://juejin.im/user/5829b958d20309005403f4d6)、小暑知秋、[大棋](https://juejin.im/user/1785262613208376/posts)、空白、qh、lvrenzhao、半节树人 等小伙伴积极试用和反馈，使潜在问题被及时发现和纳入考虑。

&nbsp;

## 谁在使用

根据小伙伴们私下反馈和调查问卷，我们了解到

包括 “腾讯音乐、BMW、TCL” 在内诸多知名厂商软件，都参考过我们开源的 [Jetpack MVVM Scaffold](https://github.com/KunMinX/Jetpack-MVVM-Scaffold) 架构模式，及正在使用我们维护的 UnPeek-LiveData 等框架。

“问卷调查” 我们长期保持对外开放，如有意可自行登记，以吸引更多小伙伴参与使用和反馈，集众人之所长，让架构组件得以不断演化和升级。

https://wj.qq.com/s2/8362688/124a/

| 集团 / 公司          | 产品           |
| -------------------- | -------------- |
| 腾讯音乐             | QQ 音乐        |
| ezen                 | Egshig音乐     |
| TCL                  | 内置文件管理器 |
| 左医科技             | 诊室听译机器人 |
| BMW                  | Speech         |
| 上海互教信息有限公司 | 知心慧学教师   |
| 美术宝               | 弹唱宝         |
|                      | 网安           |

&nbsp;

## 历史版本

| 版本                | 更新日期   |
| ------------------- | ---------- |
| UnPeekLiveData v7.2 | 2021.8.20  |
| UnPeekLiveData v7.1 | 2021.8.16  |
| UnPeekLiveData v7   | 2021.8.10  |
| UnPeekLiveData v6.1 | 2021.7.15  |
| UnPeekLiveData v6   | 2021.6.17  |
| UnPeekLiveData v5   | 2021.4.21  |
| UnPeekLiveData v4.4 | 2021.1.27  |
| UnPeekLiveData v4.2 | 2020.12.15 |
| UnPeekLiveData v4   | 2020.10.16 |
| UnPeekLiveData v3   | 2020.07.10 |
| UnPeekLiveData v2   | 2020.05.09 |
| UnPeekLiveData v1   | 2019.07.15 |

&nbsp;

## 最新更新动态

### UnPeekLiveData v7.4 特点

引入 MutableEvent/Event 类，以契合 "消息分发" 语义。

### UnPeekLiveData v7.2 特点

根据小伙伴 @[RebornWolfman](https://github.com/RebornWolfman) 在 [issue](https://github.com/KunMinX/UnPeek-LiveData/issues/21) 中分享，通过更简便方式修复 v7.0 潜在 removeObserver 内存泄漏问题。

&nbsp;

### UnPeekLiveData v7.1 特点

修复 removeObserver 潜在的内存泄漏。

&nbsp;

### UnPeekLiveData v7.0 特点

感谢小伙伴 @[RebornWolfman](https://github.com/RebornWolfman) 在 [issue](https://github.com/KunMinX/UnPeek-LiveData/issues/17) 中分享。

相较上一版，V7 版源码在 "代理类/包装类" 自行维护一个版本号，在 UnPeekLiveData 中维护一个当前版本号，且分别在 setValue 和 Observe 时机改变和对齐版本号，如此使得无需额外管理 Observer Map，从而进一步规避内存管理问题，

是继 V6 版源码以来，最简源码设计。

> 具体可参见 UnPeekLiveData 最新源码及注释说明。

&nbsp;

### UnPeekLiveData v6.1 特点

根据小伙伴 @[liweiGe](https://github.com/liweiGe) 在 [issue](https://github.com/KunMinX/UnPeek-LiveData/issues/16) 中启发，我们将 state 演变为 ObserverProxy 字段来管理，从而使 Map 合二为一，代码逻辑进一步简化。

&nbsp;

### UnPeekLiveData v6.0 特点

V6 版源码翻译和完善自小伙伴 @[wl0073921](https://github.com/wl0073921) 在 [issue](https://github.com/KunMinX/UnPeek-LiveData/issues/11) 中分享。

相比 V5 版改进之处在于，引入 Observer 代理类设计，这使页面旋屏重建时，无需通过反射方式跟踪和复用基类 Map 中 Observer，转而通过 removeObserver 方式自动移除和在页面重建后重建新 Observer，

因而复杂度由原先分散于基类数据结构，到集中在 proxy 对象这一处，进一步方便源码逻辑阅读和后续修改。

> 具体可参见 UnPeekLiveData 最新源码及注释说明。

&nbsp;

### UnPeekLiveData v5.0 特点

感谢 “腾讯音乐” 小伙伴 @[zhangjianlaoda](https://github.com/zhangjianlaoda) 贡献的重构优化代码。

**该版本保留 UnPeekLiveData v4 下述几大特点**，且在适当时机基于反射等机制，彻底解决 UnPeekLiveData v4 下 Observers 无法释放、重复创建，及 foreverObserver、removeObserver 被禁用等问题，将 UnPeekLiveData 内存性能再往上提升一阶梯。

同时，该版本使 Observe 等方法方法名和形参列表与官方 API 保持一致，尽可能减少学习成本。

> 具体可参见 UnPeekLiveData 最新源码及注释说明。

&nbsp;

### UnPeekLiveData v4.0 特点

我们在 UnPeekLiveData v3.0 基础上，参考小伙伴 [Flywith24](https://github.com/Flywith24) - [WrapperLiveData](https://github.com/Flywith24/WrapperLiveData) 遍历 ViewModelStore 思路，以此提升 “防止倒灌时机” 精准度。

> 注：为在现有 AndroidX 源码背景下实现 "防倒灌机制"，**v4.0 对 Observe 方法使用做了微调**，改为分别针对 Activity/Fragment 提供 ObserveInActivity 和 ObserveInFragment 方法，具体缘由详见源码注释说明。

&nbsp;


### UnPeekLiveData v3.0

Update since 2020.7.10

通过 **独创 “延时自动清理消息” 设计**，满足：

> 1.消息被分发给多个观察者时，**不会因第一个观察者消费过而直接被置空**

> 2.时限到，**消息便不再会被倒灌**

> 3.时限到，**消息自动从内存中清理释放**

> 4.使非入侵设计成为可能，并结合官方 SingleLiveEvent 设计实现 **遵循开闭原则非入侵重写**。

&nbsp;

### UnPeekLiveData v2.0

Update since 2020.5

> 1.结合 Event 包装类使用，对 LiveData 类进行入侵性修改。

> 2.提供 ProtectedUnPeekLiveData，基于访问权限控制实现 "读写分离"：支持只从 "唯一可信源"（例如 ViewModel）内部发送、而 Activity/Fragment 只允许 Observe。

&nbsp;

### UnPeekLiveData v1.0

Update since 2019

> 1.针对 **“二次进入二级页面时收到旧数据推送” 情况** 创建 “数据倒灌” 定义，并于网上交流传播。

> 2.参考美团 LiveDataBus 设计，透过反射方式拦截并修改 Last Version 来防止倒灌。

&nbsp;


## License

本文以 [CC 署名-非商业性使用-禁止演绎 4.0 国际协议](https://creativecommons.org/licenses/by-nc-nd/4.0/deed.zh) 发行。

Copyright © 2019-present KunMinX

![](https://images.xiaozhuanlan.com/photo/2020/8fc6f51263babeb544bb4a7dae6cde59.jpg)

文中提到的 对 “**数据倒灌**” 一词及其现象概括、对 Event 事件包装器、反射方式、SingleLiveEvent **各自存在的缺陷** 理解，及对 UnPeekLiveData “**延迟自动清理消息**” 设计，**均属于本人独立原创成果**，本人对此享有最终解释权。

文中 "最新更新动态" 处提到新版源码设计，及对新版源码思路理解和解析，属于参与过新版设计讨论有效贡献者及本人共同成果，我们对此享有所有权和最终解释权。

任何个人或组织在引用上述内容时，**须注明原作者和链接出处**。未经授权不得用于洗稿、广告包装、卖课等商业用途。

```
Copyright 2019-present KunMinX

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
