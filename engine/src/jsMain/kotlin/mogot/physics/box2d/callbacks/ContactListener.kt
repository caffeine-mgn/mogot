package mogot.physics.box2d.callbacks

import mogot.physics.box2d.collision.Manifold
import mogot.physics.box2d.dynamics.contacts.Contact

actual external interface ContactListener {
    actual fun beginContact(contact: Contact)
    actual fun endContact(contact: Contact)
    /**
     * This is called after a contact is updated. This allows you to inspect a
     * contact before it goes to the solver. If you are careful, you can modify the
     * contact manifold (e.g. disable contact).
     * A copy of the old manifold is provided so that you can detect changes.
     * Note: this is called only for awake bodies.
     * Note: this is called even when the number of contact points is zero.
     * Note: this is not called for sensors.
     * Note: if you set the number of contact points to zero, you will not
     * get an EndContact callback. However, you may get a BeginContact callback
     * the next step.
     * Note: the oldManifold parameter is pooled, so it will be the same object for every callback
     * for each thread.
     * @param contact
     * @param oldManifold
     */
    actual fun preSolve(contact: Contact, oldManifold: Manifold)

    /**
     * This lets you inspect a contact after the solver is finished. This is useful
     * for inspecting impulses.
     * Note: the contact manifold does not include time of impact impulses, which can be
     * arbitrarily large if the sub-step is small. Hence the impulse is provided explicitly
     * in a separate data structure.
     * Note: this is only called for contacts that are touching, solid, and awake.
     * @param contact
     * @param impulse this is usually a pooled variable, so it will be modified after
     * this call
     */
    actual fun postSolve(contact: Contact, impulse: ContactImpulse)
}