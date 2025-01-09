package com.thewinterframework.command;

import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.command.CommandActor;

public interface CommandSuggestions<A extends CommandActor, T> extends SuggestionProvider<A> {}
