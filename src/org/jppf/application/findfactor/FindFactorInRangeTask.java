package org.jppf.application.findfactor;

import org.jppf.server.protocol.JPPFTask;

/**
 * A job unit that can be run in a thread
 */
public class FindFactorInRangeTask extends JPPFTask implements Runnable{
    private final long begin, end, toFind;

    public FindFactorInRangeTask(long toFind, long begin, long end){
        // 1 <= begin < end <= toFind + 1
        if(!(1 <= begin)) throw new IllegalArgumentException();
        if(!(begin < end)) throw new IllegalArgumentException();
        if(!(end <= toFind+1)) throw new IllegalArgumentException();
        this.toFind = toFind;
        this.begin = begin;
        this.end = end;
        return;
    }

    @Override
    public void run() {
        long cnt = 0;
        for(long i = begin ; i < end ; i++)
            if(toFind % i == 0)
                cnt++;
        setResult(cnt);
        return;
    }

}
