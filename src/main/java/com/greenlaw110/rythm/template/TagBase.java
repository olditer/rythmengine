package com.greenlaw110.rythm.template;

import com.greenlaw110.rythm.runtime.ITag;

/**
 * Created by IntelliJ IDEA.
 * User: luog
 * Date: 25/01/12
 * Time: 12:16 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class TagBase extends TemplateBase implements ITag {
    @Override
    public String render() {
        // tag render should not reset output buffer
        return build().toString();
    }
}