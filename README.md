<h1 align="center">
  GlobalQuake
</h1>
<p align="center">
<a href="https://github.com/xspanger3770/GlobalQuake/releases"><img src="https://img.shields.io/github/release/xspanger3770/GlobalQuake.svg?style=for-the-badge&logo=github" alt="发布版本"></a> <a href="https://github.com/xspanger3770/GlobalQuake/releases"><img src="https://img.shields.io/github/downloads/xspanger3770/GlobalQuake/total?style=for-the-badge&logo=github" alt="下载量"></a> <a href="https://discord.gg/aCyuXfTyma"><img src="https://img.shields.io/badge/discord-立即加入-blue?logo=discord&style=for-the-badge" alt="Discord"></a>
</p>

[English](https://github.com/muwenyan521/GlobalQuake-Chinese/blob/v0.11.0_pre-2-7/README_en.md)

![GlobalQuake v0.10.0](https://github.com/xspanger3770/GlobalQuake/assets/100421968/d38a0596-0242-4fe9-9766-67a486832364)
<div style="display: grid; grid-template-columns: 1fr 1fr;">
<img alt="测站管理器" title="测站管理器" src="https://github.com/xspanger3770/GlobalQuake/assets/100421968/a37319ec-2132-426a-b095-2e6a9e064322" style="width: 49%; height: auto;" />
<img alt="测站管理器" title="测站管理器" src="https://i.imgur.com/T1tmMtN.png" style="width: 49%; height: auto;" />
</div>
## 简介

GlobalQuake 是一款基于 Java 的全球地震活动近实时监测软件.

用户可以从公开可用的地震台网中选择地震测站,这些测站通过 `FDSNWS 服务` 下载,并通过公开的 `Seedlink 服务器` 提供实时数据.
程序利用这些数据推算地震,并在交互式三维地球上进行可视化展示.
此外,它还可以使用多种数值估算方法来评估地震震级.这些方法能够以合理的精度估算几乎任何规模的地震震级(尤其对 M8 以下的地震估算最为准确).

> [!IMPORTANT]<br>
> 请注意,GlobalQuake 仍处于实验阶段,仅供娱乐用途,因为显示的信息可能不准确或完全错误.
>
> 同时请注意,在某些国家,在公共场所播放系统内置的某些警报音效可能被视为制造恐慌,属于违法行为.

> [!NOTE]<br>
> GlobalQuake 不拥有任何形式的数据,相关数据所有者可能随时停止通过 `Seedlink 服务器`和/或 `FDSNWS` 共享数据,恕不另行通知.

### GlobalQuake 的优势

* 便捷选择公开可用的地震测站
* 在三维交互式地球上直观显示监测到的地震
* 快速估算小型和中等规模地震的震级、位置和深度

### GlobalQuake 的局限性

* 较大规模的地震(M6+)常常触发误报或显示重复的地震事件
* 无法在短时间内探测同一震中附近发生的多次地震
* 对于给定测站组合,远震的计算精度总是低于近震

## 配置要求

- 配置要求会随你选择的测站数量而变化,包括 RAM、CPU 和带宽.
- 若仅监测所在地区的地震；性能较低的系统上也可运行 GlobalQuake.如果性能足够,也可以选择全球数百甚至数千个测站.
- 粗略估计,6 核 CPU 和 5 Mbit 网络连接应该足以处理约 1000 个测站.
- 更新:从 0.11.0 版本开始,运行 5000 个测站只需要约 2 GB 的 RAM.
- 如果 GlobalQuake 在运行几分钟后开始严重卡顿甚至崩溃,可能是由于系统 RAM 不足,需要减少选择的测站数量.

## 安装

有关安装应用程序和所需软件的指南,请参阅:[教程](https://github.com/xspanger3770/GlobalQuake/wiki/Downloads-And-Installation)

## 直播

你还可以在 YouTube 上观看我们的直播 [点击这里](https://www.youtube.com/channel/UCZmcd4cQ2H_ELWAuUdOMgRQ/live).

## 贡献

如果你考虑为项目做出贡献,请确保你已阅读 [贡献指南](https://github.com/xspanger3770/GlobalQuake/blob/main/CONTRIBUTING.md).

## 项目许可

本项目根据 MIT 许可证的条款发布.

## 特别感谢

![JQuake](https://images.weserv.nl/?url=avatars.githubusercontent.com/u/26931126?v=4&h=20&w=20&fit=cover&mask=circle&maxage=7d) [François Le Neindre](https://github.com/fleneindre) ([JQuake](https://jquake.net/en/)) - 布局、震度等级、声音警报等灵感来源  
![Philip Crotwell](https://images.weserv.nl/?url=avatars.githubusercontent.com/u/127367?v=4&h=20&w=20&fit=cover&mask=circle&maxage=7d) [Philip Crotwell](https://github.com/crotwell/) ([seisFile](http://crotwell.github.io/seisFile/), [TauP](http://crotwell.github.io/TauP/)) - 优秀且易用的库.GlobalQuake 的实现离不开这些库  
![Yacine Boussoufa](https://images.weserv.nl/?url=avatars.githubusercontent.com/u/46266665?v=4&h=20&w=20&fit=cover&mask=circle&maxage=7d) [Yacine Boussoufa](https://github.com/YacineBoussoufa/) ([EarthquakeDataCenters](https://github.com/YacineBoussoufa/EarthquakeDataCenters)) - Seedlink 和 FDSNWS 测站数据提供者

### 贡献者

<a href="https://github.com/xspanger3770/GlobalQuake/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=xspanger3770/GlobalQuake" />
</a>
