# Custom OHLC for MotiveWave

修改自 MotiveWave 自带的 Overlay - OHLC。新增了 Pre-market high and low price。由于 IBKR 数据在 8:00 AM ET 附近会有大波动，忽略了 8:00 AM - 8:05 AM 的数据。

例如下图 PML 忽略了美东时间早 8 点的大波动，显示为 677.51

<img width="384" height="268" alt="Screenshot 2026-03-08 at 4 24 42 PM" src="https://github.com/user-attachments/assets/2a2d752e-5cb6-46c6-aad2-4ec62d031c1e" />

## 编译

需要本地安装 [JDK](https://www.oracle.com/java/technologies/downloads/)。

```base
javac -cp "build/stubs_out:/Applications/MotiveWave.app/Contents/Java/mwave_sdk.jar" -d out CustomOHLC.java && \
cd out && jar cf ../CustomOHLC.jar com/ && cd ..
```
