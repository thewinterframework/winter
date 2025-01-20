package com.thewinterframework.component.tag;

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

/**
 * Represents an entity that can be tagged.
 */
public interface TagEntity {

	/**
	 * Gets the tag resolver for this entity.
	 *
	 * @param id the id of the entity
	 * @return the tag resolver
	 */
	TagResolver resolver(String id);

	/**
	 * Gets the tag resolver for this entity.
	 * @param id the id of the entity
	 * @param value the value of the entity
	 * @return the tag resolver key
	 */
	 static String placeholder(String id, String value) {
		 return id + "_" + value;
	 }

}
