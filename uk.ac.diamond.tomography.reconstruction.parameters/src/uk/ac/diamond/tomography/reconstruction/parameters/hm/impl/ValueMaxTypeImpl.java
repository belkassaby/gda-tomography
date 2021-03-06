/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.diamond.tomography.reconstruction.parameters.hm.impl;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.TypeType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.ValueMaxType;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Value Max Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ValueMaxTypeImpl#getType <em>Type</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ValueMaxTypeImpl#getPercent <em>Percent</em>}</li>
 *   <li>{@link uk.ac.diamond.tomography.reconstruction.parameters.hm.impl.ValueMaxTypeImpl#getPixel <em>Pixel</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ValueMaxTypeImpl extends EObjectImpl implements ValueMaxType {
	/**
	 * The cached value of the '{@link #getType() <em>Type</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getType()
	 * @generated
	 * @ordered
	 */
	protected TypeType type;

	/**
	 * The default value of the '{@link #getPercent() <em>Percent</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPercent()
	 * @generated
	 * @ordered
	 */
	protected static final int PERCENT_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getPercent() <em>Percent</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPercent()
	 * @generated
	 * @ordered
	 */
	protected int percent = PERCENT_EDEFAULT;

	/**
	 * This is true if the Percent attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean percentESet;

	/**
	 * The default value of the '{@link #getPixel() <em>Pixel</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPixel()
	 * @generated
	 * @ordered
	 */
	protected static final int PIXEL_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getPixel() <em>Pixel</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPixel()
	 * @generated
	 * @ordered
	 */
	protected int pixel = PIXEL_EDEFAULT;

	/**
	 * This is true if the Pixel attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean pixelESet;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ValueMaxTypeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return HmPackage.Literals.VALUE_MAX_TYPE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public TypeType getType() {
		return type;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetType(TypeType newType, NotificationChain msgs) {
		TypeType oldType = type;
		type = newType;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HmPackage.VALUE_MAX_TYPE__TYPE, oldType, newType);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setType(TypeType newType) {
		if (newType != type) {
			NotificationChain msgs = null;
			if (type != null)
				msgs = ((InternalEObject)type).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HmPackage.VALUE_MAX_TYPE__TYPE, null, msgs);
			if (newType != null)
				msgs = ((InternalEObject)newType).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HmPackage.VALUE_MAX_TYPE__TYPE, null, msgs);
			msgs = basicSetType(newType, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.VALUE_MAX_TYPE__TYPE, newType, newType));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getPercent() {
		return percent;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setPercent(int newPercent) {
		int oldPercent = percent;
		percent = newPercent;
		boolean oldPercentESet = percentESet;
		percentESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.VALUE_MAX_TYPE__PERCENT, oldPercent, percent, !oldPercentESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetPercent() {
		int oldPercent = percent;
		boolean oldPercentESet = percentESet;
		percent = PERCENT_EDEFAULT;
		percentESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, HmPackage.VALUE_MAX_TYPE__PERCENT, oldPercent, PERCENT_EDEFAULT, oldPercentESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetPercent() {
		return percentESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getPixel() {
		return pixel;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setPixel(int newPixel) {
		int oldPixel = pixel;
		pixel = newPixel;
		boolean oldPixelESet = pixelESet;
		pixelESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HmPackage.VALUE_MAX_TYPE__PIXEL, oldPixel, pixel, !oldPixelESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetPixel() {
		int oldPixel = pixel;
		boolean oldPixelESet = pixelESet;
		pixel = PIXEL_EDEFAULT;
		pixelESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, HmPackage.VALUE_MAX_TYPE__PIXEL, oldPixel, PIXEL_EDEFAULT, oldPixelESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetPixel() {
		return pixelESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case HmPackage.VALUE_MAX_TYPE__TYPE:
				return basicSetType(null, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case HmPackage.VALUE_MAX_TYPE__TYPE:
				return getType();
			case HmPackage.VALUE_MAX_TYPE__PERCENT:
				return getPercent();
			case HmPackage.VALUE_MAX_TYPE__PIXEL:
				return getPixel();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case HmPackage.VALUE_MAX_TYPE__TYPE:
				setType((TypeType)newValue);
				return;
			case HmPackage.VALUE_MAX_TYPE__PERCENT:
				setPercent((Integer)newValue);
				return;
			case HmPackage.VALUE_MAX_TYPE__PIXEL:
				setPixel((Integer)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case HmPackage.VALUE_MAX_TYPE__TYPE:
				setType((TypeType)null);
				return;
			case HmPackage.VALUE_MAX_TYPE__PERCENT:
				unsetPercent();
				return;
			case HmPackage.VALUE_MAX_TYPE__PIXEL:
				unsetPixel();
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case HmPackage.VALUE_MAX_TYPE__TYPE:
				return type != null;
			case HmPackage.VALUE_MAX_TYPE__PERCENT:
				return isSetPercent();
			case HmPackage.VALUE_MAX_TYPE__PIXEL:
				return isSetPixel();
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (percent: ");
		if (percentESet) result.append(percent); else result.append("<unset>");
		result.append(", pixel: ");
		if (pixelESet) result.append(pixel); else result.append("<unset>");
		result.append(')');
		return result.toString();
	}

} //ValueMaxTypeImpl
