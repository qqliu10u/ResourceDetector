package org.qcode.resourcedetector.detect;

import org.qcode.resourcedetector.base.observable.Observable;
import org.qcode.resourcedetector.detect.interfaces.IDetectEventListener;
import org.qcode.resourcedetector.detect.interfaces.IDetectHandler;

/***
 * author: author
 * created at 2017/7/30
 */
public abstract class AbsDetectHandler
        extends Observable<IDetectEventListener>
        implements IDetectHandler {
    //do nothing
}
