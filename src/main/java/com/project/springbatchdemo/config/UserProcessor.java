package com.project.springbatchdemo.config;

import com.project.springbatchdemo.entity.UserEntity;
import org.springframework.batch.item.ItemProcessor;

/**
 * The {@code UserProcessor} class is an implementation of the Spring Batch
 * {@link org.springframework.batch.item.ItemProcessor} interface.
 * <p>
 * This processor takes a {@link UserEntity} object as input,
 * converts the first name and last name to uppercase,
 * and returns the modified {@code UserEntity}.
 * </p>
 */
public class UserProcessor implements ItemProcessor<UserEntity, UserEntity> {

    /**
     * Processes a {@link UserEntity} by converting its first name and last name
     * to uppercase letters.
     *
     * @param userItem the {@code UserEntity} object to be processed
     * @return the updated {@code UserEntity} with uppercase first and last names
     * @throws Exception if any error occurs during processing
     */
    @Override
    public UserEntity process(UserEntity userItem) throws Exception {
        userItem.setFirstName(userItem.getFirstName().toUpperCase());
        userItem.setLastName(userItem.getLastName().toUpperCase());
        return userItem;
    }
}

