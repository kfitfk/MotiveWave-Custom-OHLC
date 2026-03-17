package com.motivewave.platform.study.overlay;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import com.motivewave.platform.sdk.common.Bar;
import com.motivewave.platform.sdk.common.BarOperation;
import com.motivewave.platform.sdk.common.BarSize;
import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.DataSeries;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.DrawContext;
import com.motivewave.platform.sdk.common.Enums.BarSizeType;
import com.motivewave.platform.sdk.common.FontInfo;
import com.motivewave.platform.sdk.common.Instrument;
import com.motivewave.platform.sdk.common.NVP;
import com.motivewave.platform.sdk.common.PathInfo;
import com.motivewave.platform.sdk.common.Tick;
import com.motivewave.platform.sdk.common.TickOperation;
import com.motivewave.platform.sdk.common.Util;
import com.motivewave.platform.sdk.common.X11Colors;
import com.motivewave.platform.sdk.common.desc.BooleanDescriptor;
import com.motivewave.platform.sdk.common.desc.DiscreteDescriptor;
import com.motivewave.platform.sdk.common.desc.EnabledDependency;
import com.motivewave.platform.sdk.common.desc.FontDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.draw.Figure;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Plots the open, high and previous close values for each day */
@StudyHeader(
    namespace="com.motivewave",
    id="MyOHLC",
    rb="com.motivewave.platform.study.nls.strings",
    name="CustomOHLC",
    desc="DESC_OHLC",
    menu="Custom",
    overlay=true,
    studyOverlay=false,
    requiresBarUpdates=true)
public class CustomOHLC extends com.motivewave.platform.sdk.study.Study
{
  enum Values { DHIGH_VAL, DLOW_VAL, DMID_VAL }

  final static String FONT="font", ALIGN="align", OPEN="open", HIGH="high", DHIGH="dhigh", LOW="low", DLOW="dlow", CLOSE="close",
      POPEN="popen", PLOW="plow", PHIGH="phigh", MID="mid", DMID="dmid", OHIGH="ohigh", OLOW="olow", PMHIGH="pmhigh", PMLOW="pmlow",
      RHIGH="orHigh", RLOW="orLow",RHIGH_1="orHigh1", RLOW_1="orLow1",RHIGH_2="orHigh2", RLOW_2="orLow2",
      RHIGH_3="orHigh3", RLOW_3="orLow3",RHIGH_4="orHigh4", RLOW_4="orLow4",RHIGH_5="orHigh5", RLOW_5="orLow5",
      RANGE="range", RANGE_INT="rangeInt", RANGE_1="range1", RANGE_INT_1="rangeInt1",RANGE_2="range2", RANGE_INT_2="rangeInt2",
      RANGE_3="range3", RANGE_INT_3="rangeInt3", RANGE_4="range4", RANGE_INT_4="rangeInt4",RANGE_5="range5", RANGE_INT_5="rangeInt5",
      SHOW_ALL="showAll", MAX_PRINTS="maxPrints", RTH="rth", SHORTEN_LATEST="sl", TIMEFRAME="tf";

  final static String LEFT="L", RIGHT="R", MIDDLE="M";
  final static String VAL_RTH="R", VAL_EXT="E", VAL_CHART="C";
  final static String RANGE_MIN="MIN", RANGE_SECONDS="SEC";

  final static BarSize DAY = BarSize.getBarSize(BarSizeType.LINEAR, 1440);
  final static BarSize MIN = BarSize.getBarSize(BarSizeType.LINEAR, 1);

  @Override
  public void initialize(Defaults defaults)
  {
    var sd = createSD();
    var tab = sd.addTab(get("TAB_GENERAL"));

    List<NVP> aligns = new ArrayList();
    aligns.add(new NVP(get("LBL_LEFT"), LEFT));
    aligns.add(new NVP(get("LBL_MIDDLE"), MIDDLE));
    aligns.add(new NVP(get("LBL_RIGHT"), RIGHT));
    List<NVP> rthOptions = new ArrayList();
    rthOptions.add(new NVP(get("LBL_RTH"), VAL_RTH));
    rthOptions.add(new NVP(get("LBL_EXT"), VAL_EXT));
    rthOptions.add(new NVP(get("LBL_CHART"), VAL_CHART));
    List<NVP> rangeIntervals = new ArrayList();
    rangeIntervals.add(new NVP(get("LBL_MINUTES"), RANGE_MIN));
    rangeIntervals.add(new NVP(get("LBL_SECONDS"), RANGE_SECONDS));
    List<NVP> timeIntervals = new ArrayList();
    timeIntervals.add(new NVP(get("LBL_DAILY"), "D"));
    timeIntervals.add(new NVP(get("LBL_WEEKLY"), "W1"));
    timeIntervals.add(new NVP(get("LBL_MONTHLY"), "M1"));
    timeIntervals.add(new NVP(get("LBL_YEARLY"), "Y1"));


    var grp = tab.addGroup("", false);
    grp.addRow(new DiscreteDescriptor(TIMEFRAME, get("LBL_TIMEFRAME"), "D", timeIntervals));
    grp.addRow(new IntegerDescriptor(MAX_PRINTS, get("LBL_MAX_PRINTS"), 5, 1, 999, 1), new BooleanDescriptor(SHOW_ALL, get("LBL_SHOW_ALL"), false, false),
        new BooleanDescriptor(SHORTEN_LATEST, get("LBL_SHORTEN_LATEST"), false, false));
    grp.addRow(new DiscreteDescriptor(RTH, get("LBL_RTH_DATA"), VAL_EXT, rthOptions));
    grp.addRow(new FontDescriptor(FONT, get("LBL_FONT"), defaults.getFont(), X11Colors.BLACK, false, true, true));
    grp.addRow(new DiscreteDescriptor(ALIGN, get("LBL_LABEL_ALIGN"), RIGHT, aligns));

    grp.addRow(new IntegerDescriptor(RANGE, get("LBL_OPEN_RANGE_MIN"), 5, 1, 1440, 1), new DiscreteDescriptor(RANGE_INT, null, RANGE_MIN, rangeIntervals));

    grp.addRow(new PathDescriptor(RHIGH, get("LBL_OPEN_RANGE_HIGH_LINE"), defaults.getPurple(), 1f, new float[] {3, 3}, false, false, true));
    grp.addRow(new PathDescriptor(RLOW, get("LBL_OPEN_RANGE_LOW_LINE"), defaults.getPurple(), 1f, new float[] {3, 3}, false, false, true));
///////////////////////////////////////////////////////////////
    grp.addRow(new IntegerDescriptor(RANGE_1, get("LBL_OPEN_RANGE_MIN"), 5, 1, 1440, 1), new DiscreteDescriptor(RANGE_INT_1, null, RANGE_MIN, rangeIntervals));
    grp.addRow(new PathDescriptor(RHIGH_1, get("LBL_OPEN_RANGE_HIGH_LINE"), defaults.getPurple(), 1f, new float[] {3, 3}, false, false, true));
    grp.addRow(new PathDescriptor(RLOW_1, get("LBL_OPEN_RANGE_LOW_LINE"), defaults.getPurple(), 1f, new float[] {3, 3}, false, false, true));

    grp.addRow(new IntegerDescriptor(RANGE_2, get("LBL_OPEN_RANGE_MIN"), 5, 1, 1440, 1), new DiscreteDescriptor(RANGE_INT_2, null, RANGE_MIN, rangeIntervals));
    grp.addRow(new PathDescriptor(RHIGH_2, get("LBL_OPEN_RANGE_HIGH_LINE"), defaults.getPurple(), 1f, new float[] {3, 3}, false, false, true));
    grp.addRow(new PathDescriptor(RLOW_2, get("LBL_OPEN_RANGE_LOW_LINE"), defaults.getPurple(), 1f, new float[] {3, 3}, false, false, true));

    grp.addRow(new IntegerDescriptor(RANGE_3, get("LBL_OPEN_RANGE_MIN"), 5, 1, 1440, 1), new DiscreteDescriptor(RANGE_INT_3, null, RANGE_MIN, rangeIntervals));
    grp.addRow(new PathDescriptor(RHIGH_3, get("LBL_OPEN_RANGE_HIGH_LINE"), defaults.getPurple(), 1f, new float[] {3, 3}, false, false, true));
    grp.addRow(new PathDescriptor(RLOW_3, get("LBL_OPEN_RANGE_LOW_LINE"), defaults.getPurple(), 1f, new float[] {3, 3}, false, false, true));

    grp.addRow(new IntegerDescriptor(RANGE_4, get("LBL_OPEN_RANGE_MIN"), 5, 1, 1440, 1), new DiscreteDescriptor(RANGE_INT_4, null, RANGE_MIN, rangeIntervals));
    grp.addRow(new PathDescriptor(RHIGH_4, get("LBL_OPEN_RANGE_HIGH_LINE"), defaults.getPurple(), 1f, new float[] {3, 3}, false, false, true));
    grp.addRow(new PathDescriptor(RLOW_4, get("LBL_OPEN_RANGE_LOW_LINE"), defaults.getPurple(), 1f, new float[] {3, 3}, false, false, true));

    grp.addRow(new IntegerDescriptor(RANGE_5, get("LBL_OPEN_RANGE_MIN"), 5, 1, 1440, 1), new DiscreteDescriptor(RANGE_INT_5, null, RANGE_MIN, rangeIntervals));
    grp.addRow(new PathDescriptor(RHIGH_5, get("LBL_OPEN_RANGE_HIGH_LINE"), defaults.getPurple(), 1f, new float[] {3, 3}, false, false, true));
    grp.addRow(new PathDescriptor(RLOW_5, get("LBL_OPEN_RANGE_LOW_LINE"), defaults.getPurple(), 1f, new float[] {3, 3}, false, false, true));

    // Turn off the shading option
    for(Object descriptors : grp.getRows()) {
      for(Object desc : (Object[])descriptors) {
        if (desc instanceof PathDescriptor) ((PathDescriptor)desc).setSupportsShadeType(false);
      }
    }

    tab = sd.addTab(get("TAB_LINES"));

    grp = tab.addGroup("", false);
    grp.addRow(new PathDescriptor(OPEN, get("LBL_OPEN_LINE"), defaults.getYellowLine(), 1f, null, false, false, true));
    grp.addRow(new PathDescriptor(HIGH, get("LBL_HIGH_LINE"), defaults.getGreenLine(), 1f, null, false, false, true));
    var path = new PathDescriptor(DHIGH, get("LBL_DHIGH_LINE"), defaults.getGreenLine(), 1f, null, false, false, true);
    path.setOverrideTagDisplay(true);
    path.setContinuous(false);
    grp.addRow(path);
    grp.addRow(new PathDescriptor(MID, get("LBL_MID_LINE"), defaults.getOrange(), 1f, null, false, false, true));
    path = new PathDescriptor(DMID, get("LBL_DMID_LINE"), defaults.getOrange(), 1f, null, false, false, true);
    path.setOverrideTagDisplay(true);
    path.setContinuous(false);
    grp.addRow(path);
    grp.addRow(new PathDescriptor(LOW, get("LBL_LOW_LINE"), defaults.getRedLine(), 1f, null, false, false, true));
    path = new PathDescriptor(DLOW, get("LBL_DLOW_LINE"), defaults.getRedLine(), 1f, null, false, false, true);
    path.setOverrideTagDisplay(true);
    path.setContinuous(false);
    grp.addRow(path);
    grp.addRow(new PathDescriptor(CLOSE, get("LBL_PREV_CLOSE_LINE"), defaults.getBlueLine(), 1f, null, false, false, true));
    grp.addRow(new PathDescriptor(POPEN, get("LBL_PREV_OPEN_LINE"), defaults.getYellowLine(), 1f, null, false, false, true));
    grp.addRow(new PathDescriptor(PHIGH, get("LBL_PREV_HIGH_LINE"), defaults.getGreenLine(), 1f, new float[] {3, 3}, true, false, true));
    grp.addRow(new PathDescriptor(PLOW, get("LBL_PREV_LOW_LINE"), defaults.getRedLine(), 1f, new float[] {3, 3}, true, false, true));
    grp.addRow(new PathDescriptor(OHIGH, get("LBL_OVERNIGHT_HIGH_LINE"), defaults.getGreenLine(), 1f, new float[] {3, 3}, false, false, true));
    grp.addRow(new PathDescriptor(OLOW, get("LBL_OVERNIGHT_LOW_LINE"), defaults.getRedLine(), 1f, new float[] {3, 3}, false, false, true));
    grp.addRow(new PathDescriptor(PMHIGH, "Pre-Market High", defaults.getGreenLine(), 1f, new float[] {3, 3}, false, false, true));
    grp.addRow(new PathDescriptor(PMLOW, "Pre-Market Low", defaults.getRedLine(), 1f, new float[] {3, 3}, false, false, true));

    // Turn off the shading option
    for(Object descriptors : grp.getRows()) {
      for(Object desc : (Object[])descriptors) {
        if (desc instanceof PathDescriptor) ((PathDescriptor)desc).setSupportsShadeType(false);
      }
    }

    sd.addDependency(new EnabledDependency(false, SHOW_ALL, MAX_PRINTS));
    sd.addDependency(new EnabledDependency(FONT, ALIGN));

    var d = new EnabledDependency(RHIGH, RANGE, RANGE_INT);
    d.setSource2(RLOW);
    d.setOrCompare(true);
    sd.addDependency(d);

    var d1 = new EnabledDependency(RHIGH_1, RANGE_1, RANGE_INT_1);
    d1.setSource2(RLOW_1);
    d1.setOrCompare(true);
    sd.addDependency(d1);

    var d2 = new EnabledDependency(RHIGH_2, RANGE_2, RANGE_INT_2);
    d2.setSource2(RLOW_2);
    d2.setOrCompare(true);
    sd.addDependency(d2);

    var d3 = new EnabledDependency(RHIGH_3, RANGE_3, RANGE_INT_3);
    d3.setSource2(RLOW_3);
    d3.setOrCompare(true);
    sd.addDependency(d3);

    var d4 = new EnabledDependency(RHIGH_4, RANGE_4, RANGE_INT_4);
    d4.setSource2(RLOW_4);
    d4.setOrCompare(true);
    sd.addDependency(d4);

    var d5 = new EnabledDependency(RHIGH_5, RANGE_5, RANGE_INT_5);
    d5.setSource2(RLOW_5);
    d5.setOrCompare(true);
    sd.addDependency(d5);

    sd.addQuickSettings(TIMEFRAME, RTH, FONT, ALIGN, RANGE, RANGE_INT, RHIGH, RLOW,
    		RANGE_1, RANGE_INT_1, RHIGH_1, RLOW_1, RANGE_2, RANGE_INT_2, RHIGH_2, RLOW_2,
    		RANGE_3, RANGE_INT_3, RHIGH_3, RLOW_3, RANGE_4, RANGE_INT_4, RHIGH_4, RLOW_4, RANGE_5, RANGE_INT_5, RHIGH_5, RLOW_5);

    sd.rowAlign(RANGE, RANGE_INT);
    sd.rowAlign(RANGE_1, RANGE_INT_1);
    sd.rowAlign(RANGE_2, RANGE_INT_2);
    sd.rowAlign(RANGE_3, RANGE_INT_3);
    sd.rowAlign(RANGE_4, RANGE_INT_4);
    sd.rowAlign(RANGE_5, RANGE_INT_5);

    var desc = createRD();
    desc.setLabelSettings(TIMEFRAME, RTH);
    desc.declarePath(Values.DHIGH_VAL, DHIGH);
    desc.declarePath(Values.DMID_VAL, DMID);
    desc.declarePath(Values.DLOW_VAL, DLOW);
  }

  @Override
  public void clearState()
  {
    super.clearState();
    lines.clear(); visibleLines.clear();
  }

  @Override
  protected void calculateValues(DataContext ctx)
  {
    var series = ctx.getDataSeries();
    var bs = ctx.getDataSeries().getBarSize();
    timeframe = getSettings().getString(TIMEFRAME, "D");
    boolean clear = Util.compare(timeframe, "D") && bs.getIntervalMinutes() >= 1440;
    clear = clear ||Util.compare(timeframe, "W1") && bs.getIntervalMinutes() >= 7*1440;
    clear = clear ||Util.compare(timeframe, "M1") && bs.getIntervalMinutes() >= 30*1440;
    clear = clear ||Util.compare(timeframe, "Y1") && bs.getIntervalMinutes() >= 365*1440;

    if (clear || !series.hasData()) {
      lines.clear();
      visibleLines.clear();
      return;
    }
    if (!Util.isEmpty(lines) || calcInProgress) return; // already calculated.  Rely on onTick() to compute from here on out
    try {
      calcInProgress = true;
      var s = getSettings();
      var instr = series.getInstrument();
      int maxPrints = s.getInteger(MAX_PRINTS, 10);
      boolean showAll = s.getBoolean(SHOW_ALL, false);
      shortenLatest = s.getBoolean(SHORTEN_LATEST, false);
      font = s.getFont(FONT);
      openPath = s.getPath(OPEN);
      midPath = s.getPath(MID);
      dMidPath = s.getPath(DMID);
      highPath = s.getPath(HIGH);
      dHighPath = s.getPath(DHIGH);
      lowPath = s.getPath(LOW);
      dLowPath = s.getPath(DLOW);
      closePath = s.getPath(CLOSE);
      pOpenPath = s.getPath(POPEN);
      pHighPath = s.getPath(PHIGH);
      pLowPath = s.getPath(PLOW);
      oHighPath = s.getPath(OHIGH);
      oLowPath = s.getPath(OLOW);
      pmHighPath = s.getPath(PMHIGH);
      pmLowPath = s.getPath(PMLOW);
      rHighPath = s.getPath(RHIGH);
      rLowPath = s.getPath(RLOW);

      //////////////////////
      rHighPath1 = s.getPath(RHIGH_1);
      rLowPath1 = s.getPath(RLOW_1);
      rHighPath2 = s.getPath(RHIGH_2);
      rLowPath2 = s.getPath(RLOW_2);
      rHighPath3 = s.getPath(RHIGH_3);
      rLowPath3 = s.getPath(RLOW_3);
      rHighPath4 = s.getPath(RHIGH_4);
      rLowPath4 = s.getPath(RLOW_4);
      rHighPath5 = s.getPath(RHIGH_5);
      rLowPath5 = s.getPath(RLOW_5);








      align = s.getString(ALIGN, RIGHT);

      var r = s.getInteger(RANGE, 5);
      if (Util.compare(s.getString(RANGE_INT, RANGE_MIN), RANGE_MIN)) r *= Util.MILLIS_IN_MINUTE;
      else r *= 1000;
      range = r;
//////////////////////////////////////////////////////////////////////////////
      var r1 = s.getInteger(RANGE_1, 5);
      if (Util.compare(s.getString(RANGE_INT_1, RANGE_MIN), RANGE_MIN)) r1 *= Util.MILLIS_IN_MINUTE;
      else r1 *= 1000;
      range1 = r1;

      var r2 = s.getInteger(RANGE_2, 5);
      if (Util.compare(s.getString(RANGE_INT_2, RANGE_MIN), RANGE_MIN)) r2 *= Util.MILLIS_IN_MINUTE;
      else r2 *= 1000;
      range2 = r2;

      var r3 = s.getInteger(RANGE_3, 5);
      if (Util.compare(s.getString(RANGE_INT_3, RANGE_MIN), RANGE_MIN)) r3 *= Util.MILLIS_IN_MINUTE;
      else r3 *= 1000;
      range3 = r3;

      var r4 = s.getInteger(RANGE_4, 5);
      if (Util.compare(s.getString(RANGE_INT_4, RANGE_MIN), RANGE_MIN)) r4 *= Util.MILLIS_IN_MINUTE;
      else r4 *= 1000;
      range4 = r4;

      var r5 = s.getInteger(RANGE_5, 5);
      if (Util.compare(s.getString(RANGE_INT_5, RANGE_MIN), RANGE_MIN)) r5 *= Util.MILLIS_IN_MINUTE;
      else r5 *= 1000;
      range5 = r5;


      String rthData = s.getString(RTH, VAL_EXT);
      if (Util.compare(rthData, VAL_RTH)) rth = true;
      else if (Util.compare(rthData, VAL_CHART) && ctx.isRTH()) rth = true;
      else rth = false;

      addFigure(figure);

      var ls = Util.isEmpty(lines) ? null : lines.get(lines.size()-1);
      if (ls != null && ls.eod <= ctx.getCurrentTime()) {
        long sod = getStartOfNextPeriod(ls.sod, instr, false);
        ls = new LineSet(ls, sod, getEndOfPeriod(sod, instr, false), getStartOfPeriodRTH(sod, instr), instr);
        lines.add(ls);
      }
      else if (ls == null){
        long eod = getStartOfPeriod(series.getStartTime(series.size()-1), instr, false);
        long sod = getStartOfPeriod(series.getStartTime(0), instr, false);
        // Hack: we need the previous close, so we should have at least 2 days here
        if (eod - sod <= Util.MILLIS_IN_DAY) sod = Util.getStartOfPrevDay(sod, instr, false);
        if (!showAll) {
          // This is tricky. Need to account for weekends here.  Note: we need an extra day here to pick up the prev close etc
          long _s = getStartOfPeriod(series.getStartTime(), instr, false);
          for(int i=0; i < maxPrints; i++) {
            _s = getStartOfPrevPeriod(_s, instr, false);
          }
          if (_s > sod) sod = _s;
        }

        ls = new LineSet(null, sod, getEndOfPeriod(sod, instr, false), getStartOfPeriodRTH(sod, instr), instr);
        lines.add(ls);
      }

      try {
        boolean _rth = rth;
        if (showOLow() || showOHigh() || showPMLow() || showPMHigh()) _rth = false;
        if (range % Util.MILLIS_IN_MINUTE == 0) {
          instr.forEachBar(ls.sod, ctx.getCurrentTime() + Util.MILLIS_IN_MINUTE, MIN, _rth, new BarCalculator(instr, ls, series));
        }
        else {
          instr.forEachTick(ls.sod, ctx.getCurrentTime() + Util.MILLIS_IN_MINUTE, _rth, new TickCalculator(instr, ls, series));
        }

        // Fallback: fill PM high/low from chart DataSeries bars if 1-min bars were unavailable
        if (showPMLow() || showPMHigh()) {
          for (var l : lines) {
            if (l.pmlow == Float.MAX_VALUE || l.pmhigh == Float.MIN_VALUE) {
              // System.out.println("[PM-FALLBACK] Entering fallback for pmlow=" + l.pmlow + " pmhigh=" + l.pmhigh);
              for (int i = 0; i < series.size(); i++) {
                long barTime = series.getStartTime(i);
                if (barTime >= l.pmStart && barTime < l.pmEnd && !isAt8amET(barTime)) {
                  float bLow = series.getLow(i);
                  float bHigh = series.getHigh(i);
                  // System.out.println("[PM-FALLBACK] time=" + new java.util.Date(barTime) + " H=" + bHigh + " L=" + bLow);
                  if (bLow < l.pmlow) l.pmlow = bLow;
                  if (bHigh > l.pmhigh) l.pmhigh = bHigh;
                }
              }
            }
          }
        }
      }
      catch(Exception exc) {
        exc.printStackTrace();
      }

      // Ensure today's LineSet exists even if no bars have been processed yet
      // (e.g., RTH mode before 9:30 AM — BarCalculator never rolled over to today)
      if (!Util.isEmpty(lines)) {
        var lastLs = lines.get(lines.size()-1);
        if (lastLs.eod <= ctx.getCurrentTime()) {
          long _sod = getStartOfNextPeriod(lastLs.sod, instr, false);
          var todayLs = new LineSet(lastLs, _sod, getEndOfPeriod(_sod, instr, false), getStartOfPeriodRTH(_sod, instr), instr);
          if (!lines.contains(todayLs)) lines.add(todayLs);
        }
      }

      Collections.sort(lines); // This should already be in order
      if (!showAll && lines.size() > maxPrints) {
        while(lines.size() > maxPrints) lines.remove(0);
      }
    }
    finally {
      calcInProgress = false;
    }
  }

  @Override
  public void onTick(DataContext ctx, Tick tick)
  {
    if (calcInProgress || Util.isEmpty(lines)) return;
    var instr = ctx.getInstrument();
    var ls = lines.get(lines.size()-1);
    float p = tick.getPrice();
    long time = tick.getTime();
    if ((showOLow() || showOHigh()) && time < ls.sodRth) {
      if (p < ls.olow) ls.olow = p;
      if (p > ls.ohigh) ls.ohigh = p;
    }
    if ((showPMLow() || showPMHigh()) && time >= ls.pmStart && time < ls.pmEnd) {
      if (isAt8amET(time)) {
        // skip ticks during 8:00-8:04 AM ET
      } else {
        if (p < ls.pmlow) ls.pmlow = p;
        if (p > ls.pmhigh) ls.pmhigh = p;
      }
    }

    // Day rollover must happen BEFORE the RTH filter so that today's LineSet
    // (with PDH/PDL from yesterday) is created even from pre-market ticks.
    var series = ctx.getDataSeries();
    if (time >= ls.eod) {
      series.setPathBreak(series.size()-1, Values.DHIGH_VAL, true);
      series.setPathBreak(series.size()-1, Values.DLOW_VAL, true);
      series.setPathBreak(series.size()-1, Values.DMID_VAL, true);
      long sod = getStartOfNextPeriod(ls.sod, instr, false);
      var nextLs = new LineSet(ls, sod, getEndOfPeriod(sod, instr, false), getStartOfPeriodRTH(sod, instr), instr);
      // In RTH mode, don't initialize RTH values from pre-market ticks
      boolean inRth = !rth || time >= nextLs.sodRth;
      if (inRth) {
        nextLs.low = nextLs.high = nextLs.open = nextLs.close = p;
      }
      nextLs.olow = nextLs.ohigh = p;

      if (time >= nextLs.pmStart && time < nextLs.pmEnd && !isAt8amET(time)) {
        nextLs.pmlow = nextLs.pmhigh = p;
      }

      if (time >= nextLs.sodRth && time < nextLs.sodRth + range) {
        nextLs.rLow = nextLs.rHigh = p;}
//////////////////////////////////////////////////
      if (time >= nextLs.sodRth && time < nextLs.sodRth + range1) {
      nextLs.rLow_1 = nextLs.rHigh_1 = p;}

      if (time >= nextLs.sodRth && time < nextLs.sodRth + range2) {
      nextLs.rLow_2 = nextLs.rHigh_2 = p;}

      if (time >= nextLs.sodRth && time < nextLs.sodRth + range3) {
      nextLs.rLow_3 = nextLs.rHigh_3 = p;}

      if (time >= nextLs.sodRth && time < nextLs.sodRth + range4) {
      nextLs.rLow_4 = nextLs.rHigh_4 = p;}

      if (time >= nextLs.sodRth && time < nextLs.sodRth + range5) {
          nextLs.rLow_5 = nextLs.rHigh_5 = p;}

      if (inRth) nextLs.mid = (nextLs.low + nextLs.high)/2;
      if (!lines.contains(nextLs)) lines.add(nextLs);
      ls = nextLs;
    }

    if (rth && !instr.isInsideTradingHours(time, rth)) return;

    if (time < ls.eod) {
      if (p < ls.low) ls.low = p;
      if (p > ls.high) ls.high = p;
      if (time < ls.sodRth) {
        if (p < ls.olow) ls.olow = p;
        if (p > ls.ohigh) ls.ohigh = p;
      }
      if (time >= ls.pmStart && time < ls.pmEnd && !isAt8amET(time)) {
        if (p < ls.pmlow) ls.pmlow = p;
        if (p > ls.pmhigh) ls.pmhigh = p;
      }

      if (time >= ls.sodRth && time < ls.sodRth + range) {
        if (p < ls.rLow) ls.rLow = p;
        if (p > ls.rHigh) ls.rHigh = p;
      }
/////////////////////////
      if (time >= ls.sodRth && time < ls.sodRth + range1) {
        if (p < ls.rLow_1) ls.rLow_1 = p;
        if (p > ls.rHigh_1) ls.rHigh_1 = p;
      }
      if (time >= ls.sodRth && time < ls.sodRth + range2) {
        if (p < ls.rLow_2) ls.rLow_2 = p;
        if (p > ls.rHigh_2) ls.rHigh_2 = p;
      }
      if (time >= ls.sodRth && time < ls.sodRth + range3) {
        if (p < ls.rLow_3) ls.rLow_3 = p;
        if (p > ls.rHigh_3) ls.rHigh_3 = p;
      }
      if (time >= ls.sodRth && time < ls.sodRth + range4) {
        if (p < ls.rLow_4) ls.rLow_4 = p;
        if (p > ls.rHigh_4) ls.rHigh_4 = p;
      }
      if (time >= ls.sodRth && time < ls.sodRth + range5) {
        if (p < ls.rLow_5) ls.rLow_5 = p;
        if (p > ls.rHigh_5) ls.rHigh_5 = p;
      }



      ls.mid = (ls.low + ls.high)/2;
      ls.close = p;
      if (ls.open == Float.MIN_VALUE) ls.open = p;
    }

    series.setFloat(Values.DHIGH_VAL, ls.high);
    series.setFloat(Values.DMID_VAL, ls.mid);
    series.setFloat(Values.DLOW_VAL, ls.low);
  }

  private long getStartOfPeriod(long time, Instrument instr, boolean rth) {
    switch(timeframe) {
    case "W1":
      long sow = instr.getStartOfWeek(time, rth);
      return sow <= time ? sow : instr.getStartOfWeek(time-3*Util.MILLIS_IN_DAY, rth);
    case "M1":
      long som = instr.getStartOfMonth(time, rth);
      return som <= time ? som : instr.getStartOfMonth(time-7*Util.MILLIS_IN_DAY, rth);
    case "Y1":
      var soy = instr.getStartOfYear(time, true);
      return soy >= time ? soy : instr.getStartOfYear(time+20*Util.MILLIS_IN_DAY, true);
    }
    long sod = instr.getStartOfDay(time, rth);
    return sod <= time ? sod : instr.getStartOfDay(time-Util.MILLIS_IN_DAY, rth);
  }

  private long getStartOfNextPeriod(long time, Instrument instr, boolean rth) {
    switch(timeframe) {
    case "W1":
      return Util.getStartOfNextWeek(time, instr, rth);
    case "M1":
      return Util.getStartOfNextMonth(time, instr, rth);
    case "Y1":
      return Util.getStartOfNextYear(time, instr, rth);
    }
    return Util.getStartOfNextDay(time, instr, rth);
  }

  private long getStartOfPrevPeriod(long time, Instrument instr, boolean rth) {
    switch(timeframe) {
    case "W1":
      return Util.getStartOfPrevWeek(time, instr, rth);
    case "M1":
      return Util.getStartOfPrevMonth(time, instr, rth);
    case "Y1":
      return Util.getStartOfPrevYear(time, instr, rth);
    }
    return Util.getStartOfPrevDay(time, instr, rth);
  }

  private long getEndOfPeriod(long time, Instrument instr, boolean rth) {
    switch(timeframe) {
    case "W1":
      long eow = instr.getEndOfWeek(time, rth);
      return eow >= time ? eow : instr.getEndOfWeek(time+5*Util.MILLIS_IN_DAY, rth);
    case "M1":
      long eom = instr.getEndOfMonth(time, rth);
      return eom >= time ? eom : instr.getEndOfMonth(time+15*Util.MILLIS_IN_DAY, rth);
    case "Y1":
      long eoy = instr.getEndOfYear(time, true);
      return eoy >= time ? eoy : instr.getStartOfYear(time+20*Util.MILLIS_IN_DAY, true);
    }
    long eod = instr.getEndOfDay(time, rth);
    return eod >= time ? eod : instr.getEndOfDay(time+Util.MILLIS_IN_DAY, rth);
  }

  private long getStartOfPeriodRTH(long sopExt, Instrument instr) {
    switch(timeframe) {
    case "W1":
      var sowRth = instr.getStartOfWeek(sopExt, true);
      return sowRth >= sopExt ? sowRth : instr.getStartOfWeek(sopExt + 2*Util.MILLIS_IN_DAY, true);
    case "M1":
      var somRth = instr.getStartOfMonth(sopExt, true);
      return somRth >= sopExt ? somRth : instr.getStartOfMonth(sopExt + 7*Util.MILLIS_IN_DAY, true);
    case "Y1":
      long soyRth = instr.getStartOfYear(sopExt, true);
      return soyRth >= sopExt ? soyRth : instr.getStartOfYear(sopExt+20*Util.MILLIS_IN_DAY, true);
    }
    var sodRth = instr.getStartOfDay(sopExt, true);
    return sodRth >= sopExt ? sodRth : instr.getStartOfDay(sopExt+Util.MILLIS_IN_DAY, true);
  }

  private boolean showDHigh() { return dHighPath != null && dHighPath.isEnabled(); }
  private boolean showDLow() { return dLowPath != null && dLowPath.isEnabled(); }
  private boolean showDMid() { return dMidPath != null && dMidPath.isEnabled(); }
  private boolean showOLow() { return oLowPath != null && oLowPath.isEnabled(); }
  private boolean showOHigh() { return oHighPath != null && oHighPath.isEnabled(); }
  private boolean showPMLow() { return pmLowPath != null && pmLowPath.isEnabled(); }
  private boolean showPMHigh() { return pmHighPath != null && pmHighPath.isEnabled(); }

  private static final TimeZone ET = TimeZone.getTimeZone("America/New_York");

  /** Check if timestamp falls within 8:00-8:05 AM ET (5-minute spike window) */
  private boolean isAt8amET(long time) {
    Calendar cal = Calendar.getInstance(ET);
    cal.setTimeInMillis(time);
    return cal.get(Calendar.HOUR_OF_DAY) == 8 && cal.get(Calendar.MINUTE) <= 5;
  }

  private static final float SPIKE_THRESHOLD = 0.005f; // 0.5%

  /** Filter out spike highs at 8:00-8:05 AM ET for pre-market data.
      Uses Close as baseline since Open can also be spiked. */
  private float filterPMHigh(long time, float high, float open, float close) {
    if (isAt8amET(time) && close > 0 && (high - close) / close > SPIKE_THRESHOLD) {
      return close;
    }
    return high;
  }

  /** Filter out spike lows at 8:00-8:05 AM ET for pre-market data.
      Uses Close as baseline since Open can also be spiked. */
  private float filterPMLow(long time, float low, float open, float close) {
    if (isAt8amET(time) && close > 0 && (close - low) / close > SPIKE_THRESHOLD) {
      return close;
    }
    return low;
  }

  /** Compute the pre-market window start (4:00 AM) and end (9:30 AM) for the given RTH start day */
  private long getPreMarketStart(long sodRth, Instrument instr) {
    TimeZone tz = instr.getTimeZone();
    Calendar cal = Calendar.getInstance(tz);
    cal.setTimeInMillis(sodRth);
    cal.set(Calendar.HOUR_OF_DAY, 4);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    return cal.getTimeInMillis();
  }

  private long getPreMarketEnd(long sodRth, Instrument instr) {
    TimeZone tz = instr.getTimeZone();
    Calendar cal = Calendar.getInstance(tz);
    cal.setTimeInMillis(sodRth);
    cal.set(Calendar.HOUR_OF_DAY, 9);
    cal.set(Calendar.MINUTE, 30);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    return cal.getTimeInMillis();
  }

  // Dashed stroke for PMH/PML lines (framework does not persist line style)
  private static final java.awt.Stroke PM_STROKE = new java.awt.BasicStroke(1f, java.awt.BasicStroke.CAP_BUTT, java.awt.BasicStroke.JOIN_MITER, 10f, new float[] {6, 4}, 0f);
  private static final java.awt.Stroke PM_STROKE_SELECTED = new java.awt.BasicStroke(2f, java.awt.BasicStroke.CAP_BUTT, java.awt.BasicStroke.JOIN_MITER, 10f, new float[] {6, 4}, 0f);

  // Keep track of the latest lines
  private List<LineSet> lines = new ArrayList();
  private List<LineSet> visibleLines = new ArrayList();
  private PathInfo openPath, highPath, dHighPath, lowPath, dLowPath, closePath, midPath, dMidPath, pOpenPath, pLowPath, pHighPath, oHighPath, oLowPath, pmHighPath, pmLowPath, rHighPath, rLowPath,
  rHighPath1, rLowPath1, rHighPath2, rLowPath2, rHighPath3, rLowPath3, rHighPath4, rLowPath4, rHighPath5, rLowPath5;
  private String timeframe, align;
  private FontInfo font;
  private long range, range1, range2,range3, range4, range5;
  private boolean calcInProgress=false, rth, shortenLatest=false;
  private Lines figure = new Lines();


  // This figure draws all of the lines
  class Lines extends Figure
  {
    @Override
    public boolean contains(double x, double y, DrawContext ctx)
    {
      for(var ls : visibleLines) {
        if (ls.contains(x, y)) return true;
      }
      return false;
    }

    @Override
    public void layout(DrawContext ctx)
    {
      setBounds(ctx.getBounds());
      var series = ctx.getDataContext().getDataSeries();
      long start = series.getVisibleStartTime();
      long end = series.getVisibleEndTime();
      setBounds(ctx.getBounds());

      // Filter visible lines
      List<LineSet> _lines = new ArrayList<>();
      synchronized(lines) {
        for(var l : lines) {
          if (l.eod <= start || l.sod >= end) continue;
          _lines.add(l);
        }
      }

      for(var l : _lines) l.layout(ctx);
      visibleLines = _lines;
    }

    @Override
    public void draw(Graphics2D gc, DrawContext ctx)
    {
      if (Util.isEmpty(visibleLines)) return;
      for(var l : visibleLines) l.draw(gc, ctx);
    }
  }

  class LineSet implements Comparable<LineSet>
  {
    LineSet(LineSet prev, long sod, long eod, long sodRth, Instrument instr)
    {
      this.sod = sod; this.eod = eod; this.sodRth = sodRth;
      this.pmStart = getPreMarketStart(sodRth, instr);
      this.pmEnd = getPreMarketEnd(sodRth, instr);
      if (prev != null) {
        prevClose = prev.close;
        phigh = prev.high;
        plow = prev.low;
        popen = prev.open;
      }
    }

    boolean contains(double x, double y)
    {
      if (contains(x, y, openPath, oy)) return true;
      if (contains(x, y, highPath, hy)) return true;
      if (contains(x, y, lowPath, ly)) return true;
      if (contains(x, y, closePath, cy)) return true;
      if (contains(x, y, pOpenPath, poly)) return true;
      if (contains(x, y, pLowPath, ply)) return true;
      if (contains(x, y, pHighPath, phy)) return true;
      if (contains(x, y, oHighPath, ply)) return true;
      if (contains(x, y, oLowPath, phy)) return true;
      if (contains(x, y, pmHighPath, pmhy)) return true;
      if (contains(x, y, pmLowPath, pmly)) return true;
      if (contains(x, y, midPath, my)) return true;
      if (contains(x, y, rLowPath, rly)) return true;
      if (contains(x, y, rHighPath, rhy)) return true;
      if (contains(x, y, rLowPath1, rly1)) return true;
      if (contains(x, y, rHighPath1, rhy1)) return true;
      if (contains(x, y, rLowPath2, rly2)) return true;
      if (contains(x, y, rHighPath2, rhy2)) return true;
      if (contains(x, y, rLowPath3, rly3)) return true;
      if (contains(x, y, rHighPath3, rhy3)) return true;
      if (contains(x, y, rLowPath4, rly4)) return true;
      if (contains(x, y, rHighPath4, rhy4)) return true;
      if (contains(x, y, rLowPath5, rly5)) return true;
      if (contains(x, y, rHighPath5, rhy5)) return true;
      return false;
    }

    void layout(DrawContext ctx)
    {
      var series = ctx.getDataContext().getDataSeries();
      lx = ctx.translateTime(sod);
      rx = ctx.translateTime(eod);
      xrth = ctx.translateTime(sodRth);

      if (isLatest() && shortenLatest) {
        int _rx = ctx.translateTime(series.getEndTime());
        if (font != null && font.isEnabled()) {
          /////////////////////////////////////////////////
          float max = Util.maxFloat(open, high, low, prevClose, popen, plow, phigh, olow, pmhigh, pmlow, mid, rLow, rHigh,
        		  rLow_1, rHigh_1, rLow_2, rHigh_2, rLow_3, rHigh_3, rLow_4, rHigh_4, rLow_5, rHigh_5);
          int width = Util.strWidth("XXX: " + series.getInstrument().format(max), font.getFont());
          _rx += width + 5;
        }
        if (_rx < rx) rx = _rx;
        if (_rx < xrth) xrth = _rx;
      }

      cx = ctx.translateTime(series.getStartTime());
      if (openPath.isEnabled()) oy = ctx.translateValue(open);
      if (highPath.isEnabled() || showDHigh()) hy = ctx.translateValue(high);
      if (lowPath.isEnabled() || showDLow()) ly = ctx.translateValue(low);
      if (closePath.isEnabled() && prevClose != 0) cy = ctx.translateValue(prevClose);
      if (pOpenPath.isEnabled() && popen != 0) poly = ctx.translateValue(popen);
      if (pLowPath.isEnabled() && plow != 0) ply = ctx.translateValue(plow);
      if (pHighPath.isEnabled() && phigh != 0) phy = ctx.translateValue(phigh);
      if (showOLow() && olow != 0) oly = ctx.translateValue(olow);
      if (showOHigh() && ohigh != 0) ohy = ctx.translateValue(ohigh);
      if (showPMLow() && pmlow != Float.MAX_VALUE) pmly = ctx.translateValue(pmlow);
      if (showPMHigh() && pmhigh != Float.MIN_VALUE) pmhy = ctx.translateValue(pmhigh);
      if (midPath.isEnabled() || showDMid()) my = ctx.translateValue(mid);
      if (rLowPath.isEnabled() && rLow != Float.MAX_VALUE) rly = ctx.translateValue(rLow);
      if (rHighPath.isEnabled() && rHigh != Float.MIN_VALUE) rhy = ctx.translateValue(rHigh);



      ///////////////////////////////////////////////////////////////
      if (rLowPath1.isEnabled() && rLow_1 != Float.MAX_VALUE) rly1 = ctx.translateValue(rLow_1);
      if (rLowPath2.isEnabled() && rLow_2 != Float.MAX_VALUE) rly2 = ctx.translateValue(rLow_2);
      if (rLowPath3.isEnabled() && rLow_3 != Float.MAX_VALUE) rly3 = ctx.translateValue(rLow_3);
      if (rLowPath4.isEnabled() && rLow_4 != Float.MAX_VALUE) rly4 = ctx.translateValue(rLow_4);
      if (rLowPath5.isEnabled() && rLow_5 != Float.MAX_VALUE) rly5 = ctx.translateValue(rLow_5);
      if (rHighPath1.isEnabled() && rHigh_1 != Float.MIN_VALUE) rhy1 = ctx.translateValue(rHigh_1);
      if (rHighPath2.isEnabled() && rHigh_2 != Float.MIN_VALUE) rhy2 = ctx.translateValue(rHigh_2);
      if (rHighPath3.isEnabled() && rHigh_3 != Float.MIN_VALUE) rhy3 = ctx.translateValue(rHigh_3);
      if (rHighPath4.isEnabled() && rHigh_4 != Float.MIN_VALUE) rhy4 = ctx.translateValue(rHigh_4);
      if (rHighPath5.isEnabled() && rHigh_5 != Float.MIN_VALUE) rhy5 = ctx.translateValue(rHigh_5);


    }

    void draw(Graphics2D gc, DrawContext ctx)
    {
      int x = rth ? xrth : lx;
      drawLine(gc, ctx, openPath, x, oy, open, "O:");
      drawLine(gc, ctx, highPath, x, hy, high, "H:");
      drawLine(gc, ctx, lowPath, x, ly, low, "L:");
      if (cy != -999) drawLine(gc, ctx, closePath, x, cy, prevClose, "C:");
      if (poly != -999) drawLine(gc, ctx, pOpenPath, x, poly, popen, "PO:");
      if (ply != -999) drawLine(gc, ctx, pLowPath, lx, ply, plow, "PDL:");
      if (phy != -999) drawLine(gc, ctx, pHighPath, lx, phy, phigh, "PDH:");
      if (oly != -999) drawLine(gc, ctx, oLowPath, x, oly, olow, "OL:");
      if (ohy != -999) drawLine(gc, ctx, oHighPath, x, ohy, ohigh, "OH:");
      if (pmly != -999) drawDashedLine(gc, ctx, pmLowPath, x, pmly, pmlow, "PML:");
      if (pmhy != -999) drawDashedLine(gc, ctx, pmHighPath, x, pmhy, pmhigh, "PMH:");
      drawLine(gc, ctx, midPath, x, my, (low + high)/2, "M:");
      drawLine(gc, ctx, rLowPath, xrth, rly, rLow, "ORL1:");
      drawLine(gc, ctx, rHighPath, xrth, rhy, rHigh, "ORH1:");


      ////////////////////////////////////////////////////////////////
      drawLine(gc, ctx, rLowPath1, xrth, rly1, rLow_1, "ORL2:");
      drawLine(gc, ctx, rHighPath1, xrth, rhy1, rHigh_1, "ORH2:");
      drawLine(gc, ctx, rLowPath2, xrth, rly2, rLow_2, "ORL3:");
      drawLine(gc, ctx, rHighPath2, xrth, rhy2, rHigh_2, "ORH3:");
      drawLine(gc, ctx, rLowPath3, xrth, rly3, rLow_3, "ORL4:");
      drawLine(gc, ctx, rHighPath3, xrth, rhy3, rHigh_3, "ORH4:");
      drawLine(gc, ctx, rLowPath4, xrth, rly4, rLow_4, "ORL5:");
      drawLine(gc, ctx, rHighPath4, xrth, rhy4, rHigh_4, "ORH5:");
      drawLine(gc, ctx, rLowPath5, xrth, rly5, rLow_5, "ORL6:");
      drawLine(gc, ctx, rHighPath5, xrth, rhy5, rHigh_5, "ORH6:");


      if (isLatest()) {
        if (!highPath.isEnabled() && showDHigh()) drawLine(gc, ctx, dHighPath, cx, hy, high, "H:");
        if (!lowPath.isEnabled() && showDLow()) drawLine(gc, ctx, dLowPath, cx, ly, low, "L:");
        if (!midPath.isEnabled() && showDMid()) drawLine(gc, ctx, dMidPath, cx, my, mid, "M:");
      }
    }

    boolean isLatest() { return !lines.isEmpty() && lines.get(lines.size()-1) == this; }

    boolean contains(double x, double y, PathInfo path, int py)
    {
      if (path == null || !path.isEnabled()) return false;
      if (x < lx || x > rx) return false;
      return Math.abs(y - py) <= 6;
    }

    void drawLine(Graphics2D gc, DrawContext ctx, PathInfo path, int x, int y, float value, String prefix)
    {
      if (!path.isEnabled() || value == Float.MAX_VALUE || value == Float.MIN_VALUE) return;
      gc.setStroke(ctx.isSelected() ? path.getSelectedStroke() : path.getStroke());
      gc.setColor(path.getColor());
      var gb = ctx.getBounds();
      if (x < gb.x) x = gb.x;
      int x2 = rx > gb.x + gb.width ? gb.x+gb.width : rx;

      if (font != null && font.isEnabled()) {
        var f = font.getFont();
        String valFmt = ctx.format(value);
        var color = path.getColor();

        String lbl = prefix + valFmt;
        if (path.isShowTag()) {
          if (path.getTagFont() != null) f = path.getTagFont();
          lbl = path.getTag();
          if (lbl == null) lbl = "";
          if (path.isShowTagValue()) lbl += " " + valFmt;
          if (path.getTagTextColor() != null) color = path.getTagTextColor();
        }

        gc.setFont(f);
        gc.setColor(color);
        var fm = gc.getFontMetrics();
        int w = fm.stringWidth(lbl);
        switch(align) {
        case RIGHT:
          //if (x2 - (w+10) < x) gc.drawLine(x, y, x2, y);
          //else {
            gc.drawLine(x, y, x2-w-5, y);
            gc.drawString(lbl, x2 - w, y+fm.getAscent()/2);
          //}
          break;
        case LEFT:
          if (x2 - x < w + 5) gc.drawLine(x, y, x2, y);
          else {
            gc.drawLine(x+w+5, y, x2, y);
            gc.drawString(lbl, x, y+fm.getAscent()/2);
          }
          break;
        case MIDDLE:
          int _cx = (x + x2)/2;
          if (_cx < x + 5) gc.drawLine(x, y, x2, y);
          else {
            gc.drawLine(x, y, _cx-w/2 - 2, y);
            gc.drawString(lbl, _cx-w/2, y+fm.getAscent()/2);
            gc.drawLine(_cx+w/2+2, y, x2, y);
          }
          break;
        }
      }
      else {
        gc.drawLine(x, y, x2, y);
      }
    }

    void drawDashedLine(Graphics2D gc, DrawContext ctx, PathInfo path, int x, int y, float value, String prefix)
    {
      if (!path.isEnabled() || value == Float.MAX_VALUE || value == Float.MIN_VALUE) return;
      gc.setStroke(ctx.isSelected() ? PM_STROKE_SELECTED : PM_STROKE);
      gc.setColor(path.getColor());
      var gb = ctx.getBounds();
      if (x < gb.x) x = gb.x;
      int x2 = rx > gb.x + gb.width ? gb.x+gb.width : rx;

      if (font != null && font.isEnabled()) {
        var f = font.getFont();
        String valFmt = ctx.format(value);
        var color = path.getColor();

        String lbl = prefix + valFmt;
        if (path.isShowTag()) {
          if (path.getTagFont() != null) f = path.getTagFont();
          lbl = path.getTag();
          if (lbl == null) lbl = "";
          if (path.isShowTagValue()) lbl += " " + valFmt;
          if (path.getTagTextColor() != null) color = path.getTagTextColor();
        }

        gc.setFont(f);
        gc.setColor(color);
        var fm = gc.getFontMetrics();
        int w = fm.stringWidth(lbl);
        gc.setStroke(ctx.isSelected() ? PM_STROKE_SELECTED : PM_STROKE);
        gc.setColor(path.getColor());
        switch(align) {
        case RIGHT:
          gc.drawLine(x, y, x2-w-4, y);
          gc.setColor(color);
          gc.drawString(lbl, x2-w, y+fm.getAscent()/2);
          break;
        case LEFT:
          gc.setColor(color);
          gc.drawString(lbl, x+2, y+fm.getAscent()/2);
          gc.setStroke(ctx.isSelected() ? PM_STROKE_SELECTED : PM_STROKE);
          gc.setColor(path.getColor());
          gc.drawLine(x+w+4, y, x2, y);
          break;
        case MIDDLE:
          int _cx = (x + x2)/2;
          if (_cx < x + 5) gc.drawLine(x, y, x2, y);
          else {
            gc.drawLine(x, y, _cx-w/2 - 2, y);
            gc.drawString(lbl, _cx-w/2, y+fm.getAscent()/2);
            gc.drawLine(_cx+w/2+2, y, x2, y);
          }
          break;
        }
      }
      else {
        gc.drawLine(x, y, x2, y);
      }
    }

    @Override
    public boolean equals(Object o)
    {
      if (o == null) return false;
      if (o == this) return true;
      return ((LineSet)o).sod == sod;
    }

    @Override
    public int compareTo(LineSet o) { return Long.compare(sod, o.sod); }

    long sod, eod, sodRth, pmStart, pmEnd;
    /////////////////////////////////////////////////////
    float open=Float.MIN_VALUE, close=Float.MIN_VALUE, prevClose=Float.MIN_VALUE, high=Float.MIN_VALUE, low=Float.MAX_VALUE,
        popen=Float.MAX_VALUE, plow=Float.MAX_VALUE, phigh=Float.MIN_VALUE, olow=Float.MAX_VALUE, ohigh=Float.MIN_VALUE,
        pmhigh=Float.MIN_VALUE, pmlow=Float.MAX_VALUE,
        rHigh=Float.MIN_VALUE, rLow=Float.MAX_VALUE, mid = Float.MAX_VALUE;
    float rHigh_1=Float.MIN_VALUE, rLow_1=Float.MAX_VALUE, rHigh_2=Float.MIN_VALUE, rLow_2=Float.MAX_VALUE, rHigh_3=Float.MIN_VALUE, rLow_3=Float.MAX_VALUE, rHigh_4=Float.MIN_VALUE, rLow_4=Float.MAX_VALUE, rHigh_5=Float.MIN_VALUE, rLow_5=Float.MAX_VALUE;


    // Layout information
    int oy, hy, ly, cy=-999, poly=-999, ply=-999, phy=-999, oly=-999, ohy=-999, pmhy=-999, pmly=-999, my, rhy, rly, rhy1, rly1, rhy2, rly2, rhy3, rly3, rhy4, rly4, rhy5, rly5;
    int lx, rx, xrth, cx;
  }

  class BarCalculator implements BarOperation
  {
    BarCalculator(Instrument instr, LineSet ls, DataSeries series)
    {
      this.instr = instr;
      this.ls = ls;
      this.series = series;
    }

    @Override
    public void onBar(Bar bar)
    {
      long barStart = bar.getStartTime();
      if (barIndex < 0 || series.getEndTime(barIndex) <= bar.getStartTime()) {
        barIndex = series.findIndex(barStart);
      }

      float barLow = bar.getLow(), barHigh = bar.getHigh();
      if (barStart >= ls.eod) { // roll over to the next day
        series.setPathBreak(barIndex, Values.DHIGH_VAL, true);
        series.setPathBreak(barIndex, Values.DLOW_VAL, true);
        series.setPathBreak(barIndex, Values.DMID_VAL, true);
        long sod = getStartOfNextPeriod(ls.sod, instr, false);
        var nextLs = new LineSet(ls, sod, getEndOfPeriod(sod, instr, false), getStartOfPeriodRTH(sod, instr), instr);
        if (!rth || barStart >= nextLs.sodRth) {
          nextLs.low = barLow;
          nextLs.high = barHigh;
          nextLs.mid = (nextLs.low + nextLs.high)/2;
          nextLs.open = bar.getOpen();
          nextLs.close = bar.getClose();
        }
        nextLs.olow = barLow;
        nextLs.ohigh = barHigh;

        if (barStart >= nextLs.pmStart && barStart < nextLs.pmEnd && !isAt8amET(barStart)) {
          // System.out.println("[PM-BAR-NEW] time=" + new java.util.Date(barStart) + " rawH=" + barHigh + " rawL=" + barLow + " O=" + bar.getOpen() + " C=" + bar.getClose() + " curPMH=" + nextLs.pmhigh + " curPML=" + nextLs.pmlow);
          if (barLow < nextLs.pmlow) nextLs.pmlow = barLow;
          if (barHigh > nextLs.pmhigh) nextLs.pmhigh = barHigh;
        }

        if (barStart >= nextLs.sodRth && barStart < nextLs.sodRth + range) {
          nextLs.rLow = barLow;
          nextLs.rHigh = barHigh;
        }
///////////////////////////////////////////////
        if (barStart >= nextLs.sodRth && barStart < nextLs.sodRth + range1) {
          nextLs.rLow_1 = barLow;
          nextLs.rHigh_1 = barHigh;
        }

        if (barStart >= nextLs.sodRth && barStart < nextLs.sodRth + range2) {
          nextLs.rLow_2 = barLow;
          nextLs.rHigh_2 = barHigh;
        }
        if (barStart >= nextLs.sodRth && barStart < nextLs.sodRth + range3) {
            nextLs.rLow_3 = barLow;
            nextLs.rHigh_3 = barHigh;
          }
        if (barStart >= nextLs.sodRth && barStart < nextLs.sodRth + range4) {
            nextLs.rLow_4 = barLow;
            nextLs.rHigh_4 = barHigh;
        }
        if (barStart >= nextLs.sodRth && barStart < nextLs.sodRth + range5) {
            nextLs.rLow_5 = barLow;
            nextLs.rHigh_5 = barHigh;
        }


        if (!lines.contains(nextLs)) lines.add(nextLs);
        ls = nextLs;
      }
      else {
        if (!rth || barStart >= ls.sodRth) {
          if (barLow < ls.low) ls.low = barLow;
          if (barHigh > ls.high) ls.high = barHigh;
          if (ls.open == Float.MIN_VALUE) ls.open = bar.getOpen();
          ls.close = bar.getClose();
          ls.mid = (ls.low + ls.high)/2;
        }
        if (barStart < ls.sodRth) {
          if (barLow < ls.olow) ls.olow = barLow;
          if (barHigh > ls.ohigh) ls.ohigh = barHigh;
        }
        if (barStart >= ls.pmStart && barStart < ls.pmEnd && !isAt8amET(barStart)) {
          // System.out.println("[PM-BAR-UPD] time=" + new java.util.Date(barStart) + " rawH=" + barHigh + " rawL=" + barLow + " curPMH=" + ls.pmhigh + " curPML=" + ls.pmlow);
          if (barLow < ls.pmlow) ls.pmlow = barLow;
          if (barHigh > ls.pmhigh) ls.pmhigh = barHigh;
        }

        if (barStart >= ls.sodRth && barStart < ls.sodRth + range) {
          if (barLow < ls.rLow) ls.rLow = barLow;
          if (barHigh > ls.rHigh) ls.rHigh = barHigh;
        }

        if (barStart >= ls.sodRth && barStart < ls.sodRth + range1) {
          if (barLow < ls.rLow_1) ls.rLow_1 = barLow;
          if (barHigh > ls.rHigh_1) ls.rHigh_1 = barHigh;
        }

        if (barStart >= ls.sodRth && barStart < ls.sodRth + range2) {
          if (barLow < ls.rLow_2) ls.rLow_2 = barLow;
          if (barHigh > ls.rHigh_2) ls.rHigh_2 = barHigh;
        }
        if (barStart >= ls.sodRth && barStart < ls.sodRth + range3) {
            if (barLow < ls.rLow_3) ls.rLow_3 = barLow;
            if (barHigh > ls.rHigh_3) ls.rHigh_3 = barHigh;
          }

          if (barStart >= ls.sodRth && barStart < ls.sodRth + range4) {
            if (barLow < ls.rLow_4) ls.rLow_4 = barLow;
            if (barHigh > ls.rHigh_4) ls.rHigh_4 = barHigh;
          }

          if (barStart >= ls.sodRth && barStart < ls.sodRth + range5) {
            if (barLow < ls.rLow_5) ls.rLow_5 = barLow;
            if (barHigh > ls.rHigh_5) ls.rHigh_5 = barHigh;
          }


      }

      if (rth && barStart < ls.sodRth) return;

      // Developing values
      series.setFloat(barIndex, Values.DHIGH_VAL, ls.high);
      series.setFloat(barIndex, Values.DLOW_VAL, ls.low);
      series.setFloat(barIndex, Values.DMID_VAL, ls.mid);

      // Hack for non-linear bars, we can have a gap in the data.  If this is the case, fill in the gap (accounting for RTH data)
      if (lastIndex != -1 && barIndex - lastIndex > 1) {
        for(int i = lastIndex+1; i < barIndex; i++) {
          long start = series.getStartTime(i);
          if (rth && start < ls.sodRth) break;
          series.setFloat(i, Values.DHIGH_VAL, series.getFloat(i-1, Values.DHIGH_VAL));
          series.setFloat(i, Values.DLOW_VAL, series.getFloat(i-1, Values.DLOW_VAL));
          series.setFloat(i, Values.DMID_VAL, series.getFloat(i-1, Values.DMID_VAL));
        }
      }
      lastIndex = barIndex;
    }

    int barIndex = -1, lastIndex = -1;
    Instrument instr;
    LineSet ls;
    DataSeries series;
  }

  // TODO: this should be refactored with onTick above
  class TickCalculator implements TickOperation
  {
    TickCalculator(Instrument instr, LineSet ls, DataSeries series)
    {
      this.instr = instr;
      this.ls = ls;
      this.series = series;
    }

    @Override
    public void onTick(Tick tick)
    {
      long time = tick.getTime();
      float price = tick.getPrice();
      if (barIndex < 0 || series.getEndTime(barIndex) <= tick.getTime()) {
        barIndex = series.findIndex(tick.getTime());
      }
      if (time >= ls.eod) { // roll over to the next day
        series.setPathBreak(barIndex, Values.DHIGH_VAL, true);
        series.setPathBreak(barIndex, Values.DLOW_VAL, true);
        series.setPathBreak(barIndex, Values.DMID_VAL, true);
        long sod = getStartOfNextPeriod(ls.sod, instr, false);
        var nextLs = new LineSet(ls, sod, getEndOfPeriod(sod, instr, false), getStartOfPeriodRTH(sod, instr), instr);
        if (!rth || time >= nextLs.sodRth) {
          nextLs.low = nextLs.high = nextLs.open = nextLs.close = nextLs.ohigh = nextLs.olow = price;
          nextLs.mid = (nextLs.low + nextLs.high)/2;
        }
        nextLs.ohigh = nextLs.olow = price;

        if (time >= nextLs.pmStart && time < nextLs.pmEnd && !isAt8amET(time)) {
          nextLs.pmlow = nextLs.pmhigh = price;
        }

        if (time >= nextLs.sodRth && time < nextLs.sodRth + range) {
          nextLs.rLow = price;
          nextLs.rHigh = price;
        }

        if (time >= nextLs.sodRth && time < nextLs.sodRth + range1) {
          nextLs.rLow_1 = price;
          nextLs.rHigh_1 = price;
        }

        if (time >= nextLs.sodRth && time < nextLs.sodRth + range2) {
          nextLs.rLow_2 = price;
          nextLs.rHigh_2 = price;
        }
        if (time >= nextLs.sodRth && time < nextLs.sodRth + range3) {
            nextLs.rLow_3 = price;
            nextLs.rHigh_3 = price;
          }

          if (time >= nextLs.sodRth && time < nextLs.sodRth + range4) {
            nextLs.rLow_4 = price;
            nextLs.rHigh_4 = price;
          }

          if (time >= nextLs.sodRth && time < nextLs.sodRth + range5) {
            nextLs.rLow_5 = price;
            nextLs.rHigh_5 = price;
          }

        if (!lines.contains(nextLs)) lines.add(nextLs);
        ls = nextLs;
      }
      else {
        if (!rth || time >= ls.sodRth) {
          if (price < ls.low) ls.low = price;
          if (price > ls.high) ls.high = price;
          if (ls.open == Float.MIN_VALUE) ls.open = price;
          ls.close = price;
          ls.mid = (ls.low + ls.high)/2;
        }
        if (time < ls.sodRth) {
          if (price < ls.olow) ls.olow = price;
          if (price > ls.ohigh) ls.ohigh = price;
        }
        if (time >= ls.pmStart && time < ls.pmEnd && !isAt8amET(time)) {
          if (price < ls.pmlow) ls.pmlow = price;
          if (price > ls.pmhigh) ls.pmhigh = price;
        }

        if (time >= ls.sodRth && time < ls.sodRth + range) {
          if (price < ls.rLow) ls.rLow = price;
          if (price > ls.rHigh) ls.rHigh = price;
        }

        if (time >= ls.sodRth && time < ls.sodRth + range1) {
          if (price < ls.rLow_1) ls.rLow_1 = price;
          if (price > ls.rHigh_1) ls.rHigh_1 = price;
        }

        if (time >= ls.sodRth && time < ls.sodRth + range2) {
          if (price < ls.rLow_2) ls.rLow_2 = price;
          if (price > ls.rHigh_2) ls.rHigh_2 = price;
        }
        if (time >= ls.sodRth && time < ls.sodRth + range3) {
            if (price < ls.rLow_3) ls.rLow_3 = price;
            if (price > ls.rHigh_3) ls.rHigh_3 = price;
          }

          if (time >= ls.sodRth && time < ls.sodRth + range4) {
            if (price < ls.rLow_4) ls.rLow_4 = price;
            if (price > ls.rHigh_4) ls.rHigh_4 = price;
          }

          if (time >= ls.sodRth && time < ls.sodRth + range5) {
            if (price < ls.rLow_5) ls.rLow_5 = price;
            if (price > ls.rHigh_5) ls.rHigh_5 = price;
          }


      }
      if (rth && time < ls.sodRth) return;
      // Developing values
      series.setFloat(barIndex, Values.DHIGH_VAL, ls.high);
      series.setFloat(barIndex, Values.DLOW_VAL, ls.low);
      series.setFloat(barIndex, Values.DMID_VAL, ls.mid);
    }

    Instrument instr;
    LineSet ls;
    DataSeries series;
    int barIndex = -1;
  }
}
