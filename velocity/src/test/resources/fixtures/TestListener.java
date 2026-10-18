package example;

import com.google.inject.Inject;
import com.thewinterframework.velocity.listener.ListenerComponent;
import com.thewinterframework.wire.condition.common.expression.RequiresExpr;
import com.thewinterframework.wire.condition.common.plugin.RequiresPlugin;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;

@ListenerComponent
@RequiresPlugin("dependency")
@RequiresExpr("config.get('listeners')")
public class TestListener {
	public final TestService service;
	@Inject public TestListener(TestService service) { this.service = service; }
	@Subscribe public void disconnect(DisconnectEvent event) { }
}
