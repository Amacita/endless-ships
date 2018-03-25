import React from 'react';
import NumberFormat from 'react-number-format';
import { Link } from 'react-router-dom';
import R from 'ramda';

import LicenseLabel from './components/LicenseLabel';

const kebabCase = string => string.replace(/\s+/g, '-').toLowerCase();

const capitalize = ([first, ...rest]) => first.toUpperCase() + rest.join('').toLowerCase();

const nbsp = '\u00a0';

const nbspize = string => string.replace(/ /g, nbsp);

const FormattedNumber = ({ number, isDecimal }) => {
  return (
    <NumberFormat value={number}
                  displayType={'text'}
                  thousandSeparator={true}
                  decimalScale={2}
                  fixedDecimalScale={isDecimal} />
  );
};

const renderLicenses = (licenses = []) => {
  const labels = licenses.map(license => <LicenseLabel license={license} key={license} />);

  return R.intersperse(' ', labels);
};

const ShipLink = ({ shipName }) => (
  <Link to={`/ships/${kebabCase(shipName)}`}>{nbspize(shipName)}</Link>
);

const OutfitLink = ({ outfitName }) => (
  <Link to={`/outfits/${kebabCase(outfitName)}`}>{nbspize(outfitName)}</Link>
);

const intersperse = (arr, sep) => {
  if (arr.length === 0) {
    return [];
  } else {
    return arr.slice(1).reduce((xs, x, idx) => xs.concat([sep(idx), x]), [arr[0]]);
  }
};

const orZero = (fn) => {
  return (...args) => {
    const result = fn(...args);
    return R.isNil(result) ? 0 : result;
  };
};

const damage = R.curry((damageType, gun) => {
  return R.path(['weapon', damageType, 'perSecond'], gun);
});

const damagePerOutfitSpace = R.curry((damageType, gun) => {
  const gunDamage = damage(damageType, gun);

  if (!R.isNil(gunDamage)) {
    return gunDamage / gun.outfitSpace;
  } else {
    return null;
  }
});

export {
  FormattedNumber,
  kebabCase,
  capitalize,
  nbsp,
  nbspize,
  renderLicenses,
  ShipLink,
  OutfitLink,
  intersperse,
  orZero,
  damage,
  damagePerOutfitSpace
};
