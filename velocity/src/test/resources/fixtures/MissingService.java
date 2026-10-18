package example;

import com.thewinterframework.service.annotation.Service;
import com.thewinterframework.service.annotation.lifecycle.OnEnable;
import com.thewinterframework.wire.condition.common.plugin.RequiresPlugin;

@Service
@RequiresPlugin("missing")
public class MissingService {
	public static boolean enabled;
	@OnEnable public void enable() { enabled = true; }
}
