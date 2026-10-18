package example;

import com.thewinterframework.velocity.listener.ListenerComponent;
import com.thewinterframework.wire.condition.common.expression.RequiresExpr;

@ListenerComponent
@RequiresExpr("false")
public class DisabledListener {
	public DisabledListener() { throw new IllegalStateException("Disabled listener was instantiated"); }
}
