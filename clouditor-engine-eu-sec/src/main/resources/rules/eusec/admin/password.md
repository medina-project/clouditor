# Enforce Password Requirements

All administrators should have strong password requirements, such as a good length, use of symbols as well as mixed upper and lowercase characters.

```ccl
Administrator has passwordRequirements.minimalLength >= 10
Administrator has passwordRequirements.upperAndLowerCaseEnforcement == true
Administrator has passwordRequirements.numericCharacterEnforcement == true
Administrator has passwordRequirements.specialCharacterEnforcement == true
 ```

## Controls

- Cloud Control Matrix/IAM-12
