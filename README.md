<h1 align="center">
  GlobalQuake
</h1>

<p align="center">
  <a href="https://github.com/xspanger3770/GlobalQuake/releases"><img src="https://img.shields.io/github/release/xspanger3770/GlobalQuake.svg?style=for-the-badge&logo=github" alt="发布版本"></a> <a href="https://github.com/xspanger3770/GlobalQuake/releases"><img src="https://img.shields.io/github/downloads/xspanger3770/GlobalQuake/total?style=for-the-badge&logo=github" alt="下载量"></a> <a href="https://discord.gg/aCyuXfTyma"><img src="https://img.shields.io/badge/discord-立即加入-blue?logo=discord&style=for-the-badge" alt="Discord"></a>
</p>

![GlobalQuake v0.10.0](https://github.com/xspanger3770/GlobalQuake/assets/100421968/d38a0596-0242-4fe9-9766-67a486832364)

<div style="display: grid; grid-template-columns: 1fr 1fr;">
<img alt="台站管理器" title="台站管理器" src="https://github.com/xspanger3770/GlobalQuake/assets/100421968/a37319ec-2132-426a-b095-2e6a9e064322" style="width: 49%; height: auto;" />
<img alt="震源管理器" title="震源管理器" src="https://i.imgur.com/T1tmMtN.png" style="width: 49%; height: auto;" />
</div>

## 本地化说明

本 Fork 是基于 GlobalQuake 仓库重新修改的本地化版本。在有限能力的范围内，完成了大约 90% 的本地化。

### 目前暂未/无法本地化的内容：
- 全球地区名称（体量过大，翻译难度高）
- 中国地图（使用了@azzbm 的中国版地图，但是由于未知原因无法生效）

### 已知问题：
- 由于代码量过大，无法以 properties 形式本地化，目前暂时使用替换字符串方式。
- 为适配后续更新，考虑推出汉化包的形式解决。

> [!NOTE]<br>
> 该版本将原版字体均更换为 MiSans 系列字体，推荐下载并安装 MiSans 字体以获得更好的使用体验。（不安装亦可正常运行）

#### 因能力有限，本地化过程难免有纰漏，欢迎各位提出 Issue 以推进本地化质量！

## 简介

GlobalQuake 是一个实验性的 Java 应用程序，可用于近实时监测全球范围内的地震活动。

该系统允许用户从公开可用的地震台网中选择地震台站，这些台站通过`FDSNWS 服务`下载，并通过公开可用的`Seedlink 服务器`提供实时数据。\
程序利用这些数据来探测地震，并在交互式三维地球仪上进行可视化展示。\
此外，它还可以使用各种数值估算方法来评估地震震级，~~但目前仅适用于小型和中等规模的地震，震级上限为5或6级。~~
这些方法能够以合理的精度估算几乎任何规模的地震震级（尽管对M8以下的地震估算最为准确）

> [!重要提示]<br>
> 请注意，GlobalQuake 仍处于实验阶段，仅供娱乐用途，因为显示的信息可能不准确或完全错误。\
> \
> 同时请注意，在公共场所播放系统内置的某些警报音效在某些国家可能被视为一种恐慌制造行为，属于违法行为。

> [!注意]<br>
> GlobalQuake 不拥有任何形式的数据，相关数据所有者可能随时停止通过 Seedlink 服务器和/或 FDSNWS 共享数据，恕不另行通知。

### GlobalQuake 的优势

* 可以轻松选择公开可用的地震台站。
* 可以在三维交互式全球地图上可视化显示探测到的地震。
* 可以快速估算小型和中等规模地震的震级、位置和深度。

### GlobalQuake 的局限性

* 较大规模的地震（M6+）常常触发误报或显示重复的地震事件。
* 无法在短时间内探测同一震中附近发生的多次地震。
* 对于给定台站组合，远震的计算精度总是低于近震。

## 系统要求

- 系统要求将随您选择的台站数量而变化。这包括 RAM、CPU 和网络使用。
- 您可以在性能较低的系统上运行 GlobalQuake，仅监测您所在地区的地震，或者如果您的系统性能足够，可以选择全球数百甚至数千个台站。
- 粗略估计，~~4GB RAM~~、6 核 CPU 和 5 Mbit 网络连接应该足以处理约 1000 个台站。
- 更新：从 0.11.0 版本开始，运行 5000 个台站只需要约 2 GB 的 RAM。
- 如果 GlobalQuake 在运行几分钟后开始严重卡顿甚至崩溃，可能是由于系统 RAM 不足，您需要减少选择的台站数量。

## 安装

有关安装应用程序和所需软件的指南可以在这里找到：[教程](https://github.com/xspanger3770/GlobalQuake/wiki/Downloads-And-Installation)

## 直播

您还可以在 YouTube 上观看我们的直播 [点击这里](https://www.youtube.com/channel/UCZmcd4cQ2H_ELWAuUdOMgRQ/live)。

## 贡献

如果您考虑为项目做出贡献，请确保您已阅读 [贡献指南](https://github.com/xspanger3770/GlobalQuake/blob/main/CONTRIBUTING.md)

## 项目许可

本项目根据 MIT 许可证的条款发布。\
~~然而，请注意，此存储库包含来自其他两个项目的音效，每个项目都受其各自许可证的管辖。\
带有[`LICENSE_J`](https://github.com/xspanger3770/GlobalQuake/blob/main/LICENSE_J)标识的音效是根据其特定许可证 - [JQuake](https://jquake.net/) 的条款使用的，带有[`LICENSE_K`](https://github.com/xspanger3770/GlobalQuake/blob/main/LICENSE_K)标识的音效也受其独特许可证 - [KiwiMonitor](https://kiwimonitor.amebaownd.com/) 的约束。\
在使用或分发本项目时，务必查看并遵守这些额外的许可证。有关更多详细信息，请参阅相应的许可证文件。~~

## 特别感谢

![JQuake](https://images.weserv.nl/?url=avatars.githubusercontent.com/u/26931126?v=4&h=20&w=20&fit=cover&mask=circle&maxage=7d) [François Le Neindre](https://github.com/fleneindre) ([JQuake](https://jquake.net/en/)) - 布局、震度等级、声音警报等灵感来源\
![Philip Crotwell](https://images.weserv.nl/?url=avatars.githubusercontent.com/u/127367?v=4&h=20&w=20&fit=cover&mask=circle&maxage=7d) [Philip Crotwell](https://github.com/crotwell/) ([seisFile](http://crotwell.github.io/seisFile/), [TauP](http://crotwell.github.io/TauP/)) - 优秀且易用的库。没有这些库，GlobalQuake将无法实现\
![Yacine Boussoufa](https://images.weserv.nl/?url=avatars.githubusercontent.com/u/46266665?v=4&h=20&w=20&fit=cover&mask=circle&maxage=7d) [Yacine Boussoufa](https://github.com/YacineBoussoufa/) ([EarthquakeDataCenters](https://github.com/YacineBoussoufa/EarthquakeDataCenters)) - Seedlink和FDSNWS数据提供者列表

### 贡献者

<a href="https://github.com/xspanger3770/GlobalQuake/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=xspanger3770/GlobalQuake" />
</a>