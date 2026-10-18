package example;

import com.thewinterframework.velocity.listener.ListenerComponent;
import com.thewinterframework.wire.condition.common.plugin.RequiresPlugin;

@ListenerComponent
@RequiresPlugin("missing")
public class MissingListener {
	public MissingListener() { throw new IllegalStateException("Absent listener was instantiated"); }
}
