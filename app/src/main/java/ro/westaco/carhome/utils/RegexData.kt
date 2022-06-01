package ro.westaco.carhome.utils

class RegexData {

    companion object {

        fun checkFirstLastNameRegex(str: String): Boolean {
            val regex =
                Regex("[0-9!@#\$€£%&*'.,;:)(?!/+_<>|\\[\\]{}\\\\]*")
            return str.matches(regex)
        }

        fun checkCUIRegex(cuiNumber: String): Boolean {
            val regex =
                Regex("^[A-Z]{0,2}[0-9]{2,13}")
            return cuiNumber.matches(regex)
        }

        fun checkRegCompanyRegex(cuiNumber: String): Boolean {
            if (cuiNumber.isEmpty())
                return true
            val regex =
                Regex("^[A-Z]{0,2}[0-9]{2,13}")
            return cuiNumber.matches(regex)
        }

        fun checkCNPNumberIsValid(cnpNumber: String): Boolean {

            /// First it will check for valid CNP number with regex.
            /// If it is valid CNP number then then continue with flow otherwise return false
            if (!checkCNPRegex(cnpNumber)) {
                return false
            }

            val cnpData = cnpNumber
            /// var cnpConstantData is an static key which is used for validating CNP number.
            val cnpConstantData = "279146358279"


            /// 1. We will convert string(cnpData) and string(cnpConstantData) into array of single digit.
            val cnpArr = ArrayList<String>(cnpData.length)
            for (c in cnpData) {
                cnpArr.add(c.toString())
            }
            val cnpConstantDataArr = ArrayList<String>(cnpConstantData.length)
            for (c in cnpConstantData) {
                cnpConstantDataArr.add(c.toString())
            }


            /// 2. Store last index value of array(cnpArr) in an variable for later use, then remove last index of array(cnpArr)
            val lastDigit = cnpArr[cnpArr.size - 1]
            cnpArr.removeAt(cnpArr.size - 1)


            /// 3. We will multiply each element of both array in array(arrayMultiplication)
            val arrayMultiplication =
                cnpArr.zip(cnpConstantDataArr) { i, j -> i.toInt() * j.toInt() }

            /// 4. We will addition of each array elements.(sum)
            val sum = arrayMultiplication.sum()

            /// 5. We modules sum of array with 11
            val modules = sum % 11

            /// 6. We compare ((modules value with earlier saved lastDigit variable) and (modules value is  less then 10)) if its match then CNP number is valid, there was one more senario if ((modules value equal to 10) and (last digit equal 1)) then CNP number is valid.Otherwise CNP numbewr is invalid.
            return (modules < 10 && modules == lastDigit.toInt()) || (modules == 10 && lastDigit.toInt() == 1)
        }

        private fun checkCNPRegex(cnpNumber: String): Boolean {
            val regex =
                Regex("^[1-8]\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[+01])(0[1-9]|[1-4]\\d|5[0-2]|99)\\d{4}$")
            return cnpNumber.matches(regex)
        }

        fun checkNumberPlateROU(licenseNumberROU: String): Boolean {

            if (licenseNumberROU.isEmpty())
                return true

            val regex =
                Regex("((AB|AG|AR|BC|BH|BN|BR|BT|BV|BZ|CJ|CL|CS|CT|CV|DB|DJ|GJ|GL|GR|HD|HR|IF|IL|IS|MH|MM|MS|NT|OT|PH|SB|SJ|SM|SV|TL|TM|TR|VL|VN|VS)([0][1-9]|[1-9]\\d)([A-PR-Z]){3})|((B)([0][1-9]|[1-9]\\d{1,2})([A-PR-Z]){3})|((AB|AG|AR|B|BC|BH|BN|BR|BT|BV|BZ|CJ|CL|CS|CT|CV|DB|DJ|GJ|GL|GR|HD|HR|IF|IL|IS|MH|MM|MS|NT|OT|PH|SB|SJ|SM|SV|TL|TM|TR|VL|VN|VS|CD|CO|TC)[1-9]\\d{2,10})")
            return licenseNumberROU.matches(regex = regex)
        }

        fun checkNumberPlateQAT(licenseNumberQAT: String): Boolean {

            if (licenseNumberQAT.isEmpty())
                return true

            val regex = Regex("([A-Z]{1,2}([0-9]{4})([A-Z]){2})")

            return licenseNumberQAT.matches(regex = regex)
        }

        fun checkNumberPlateUKR(licenseNumberUKR: String): Boolean {

            if (licenseNumberUKR.isEmpty())
                return true

            val regex =
                Regex("((AB|AC|AE|AH|AM|AO|AP|AT|AI|AA|BA|BB|BC|BE|BH|BI|BK|BM|BO|AX|BT|BX|CA|CB|CE)([0-9]{4})([A-Z]){2})")

            return licenseNumberUKR.matches(regex = regex)
        }

        fun checkNumberPlateBGR(licenseNumberBGR: String): Boolean {

            if (licenseNumberBGR.isEmpty())
                return true

            val regex =
                Regex("((E|A|AA|TX|EB|X|Y|K|KH|OB|M|PA|PK|EH|PB|PP|P|H|CC|CH|CM|C|CA|CB|CO|CT|T|B|BT|BH|BP)([0-9]{4})([A-Z]){2})")

            return licenseNumberBGR.matches(regex = regex)
        }

    }
}