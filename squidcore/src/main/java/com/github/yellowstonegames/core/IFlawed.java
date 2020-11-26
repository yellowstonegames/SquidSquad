package com.github.yellowstonegames.core;

/**
 * An empty marker interface to indicate that an implementor has known or intentional issues with a key property of its
 * functionality. This is almost always combined with another interface, and used to indicate that a class that
 * implements both IFlawed and a "normal" interface implements the second one abnormally. Typically, you would use a
 * flawed implementation to compare with a non-flawed one, or because the flaws have aesthetic merit from their biases.
 */
public interface IFlawed {
}
