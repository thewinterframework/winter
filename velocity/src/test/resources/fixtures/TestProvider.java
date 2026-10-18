package example;

import com.google.inject.Provider;
import com.thewinterframework.provider.annotation.ProviderComponent;

@ProviderComponent
public class TestProvider implements Provider<String> {
	public String get() { return "provided"; }
}
