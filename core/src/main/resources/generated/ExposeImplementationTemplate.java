package <PACKAGE>;

import com.google.inject.Inject;
import com.google.inject.Injector;

public class Default<PLUGIN>API implements <PLUGIN>API {
	private final Injector injector;

	@Inject
	public Default<PLUGIN>API(final Injector injector) {
		this.injector = injector;
	}

	<el>
		@Override
		public <TYPE> get<TYPE_NAME>() {
			return injector.getInstance(<TYPE>.class);
		}
	</el>
}